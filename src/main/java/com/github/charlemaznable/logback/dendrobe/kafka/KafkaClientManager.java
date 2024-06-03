package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.helpers.Reporter;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaBatchClient.closeClient;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaBatchClient.startClient;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaBatchClient.stopClient;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class KafkaClientManager {

    private static final Map<String, KafkaBatchClient> kafkaClients = new ConcurrentHashMap<>();
    private static final Map<String, String> kafkaConfigs = new ConcurrentHashMap<>();
    private static final AsyncEventBus eventBus;
    private static final CopyOnWriteArrayList<KafkaClientManagerListener> listeners = new CopyOnWriteArrayList<>();
    private static final AsyncEventBus notifyBus;

    static {
        eventBus = new AsyncEventBus(KafkaClientManager.class.getName(), newFixedThreadPool(1));
        eventBus.register(new Object() {
            @Subscribe
            public void configKafkaClient(String kafkaName) {
                // 内部配置的KafkaClient
                val configValue = KafkaConfigServiceElf
                        .configService().getKafkaConfigValue(kafkaName);
                if (isNull(configValue)) {
                    // 不存在对应的配置
                    // 移除配置缓存
                    val previousConfig = kafkaConfigs.remove(kafkaName);
                    // 缓存不存在, 表示之前的KafkaClient不存在/为外部导入的KafkaClient, 则直接返回
                    if (isNull(previousConfig)) return;
                    // 缓存存在, 表示之前的KafkaClient为内部配置的KafkaClient
                    // 则此处需要移除此内部配置的KafkaClient
                    // 移除内部配置的KafkaClient实例并关闭
                    closeClient(stopClient(kafkaClients.remove(kafkaName)));
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configValue.equals(kafkaConfigs.get(kafkaName))) return;
                // 校验配置
                val kafkaConfig = KafkaConfigServiceElf.configService()
                        .parseKafkaConfig(kafkaName, configValue);
                val batchConfig = KafkaConfigServiceElf.configService()
                        .parseBatchExecutorConfig(kafkaName, configValue);
                // 不论之前的KafkaClient是内部配置的还是外部导入的
                // 此处都需要以当前的内部配置KafkaClient覆盖之
                // 保存配置缓存
                val previousConfig = kafkaConfigs.put(kafkaName, configValue);
                // 移除之前的KafkaClient实例
                val previous = stopClient(kafkaClients.remove(kafkaName));
                // 缓存存在, 表示之前的KafkaClient为内部配置的KafkaClient
                // 则此处需要同步关闭, 对外部导入的KafkaClient不做操作
                if (nonNull(previousConfig)) closeClient(previous);
                // 同步新建KafkaClient实例并加入
                kafkaClients.put(kafkaName, startClient(kafkaConfig, batchConfig));
                notifyBus.post(kafkaName);
            }

            @Subscribe
            public void configKafkaClient(ExternalKafkaClient externalKafkaClient) {
                val kafkaName = externalKafkaClient.getKafkaName();
                // 不论之前的KafkaClient是内部配置的还是外部导入的
                // 此处都需要以当前的外部导入KafkaClient覆盖之
                // 清除同名配置缓存
                val previousConfig = kafkaConfigs.remove(kafkaName);
                // 移除之前的KafkaClient实例
                val previous = stopClient(kafkaClients.remove(kafkaName));
                // 缓存存在, 表示之前的KafkaClient为内部配置的KafkaClient
                // 则此处需要同步关闭, 对外部导入的KafkaClient不做操作
                if (nonNull(previousConfig)) closeClient(previous);
                // 加入新的KafkaClient实例
                val producer = externalKafkaClient.getProducer();
                val batchConfig = externalKafkaClient.getBatchConfig();
                if (nonNull(producer)) {
                    kafkaClients.put(kafkaName, startClient(producer, batchConfig));
                    notifyBus.post(kafkaName);
                }
            }
        });

        notifyBus = new AsyncEventBus(KafkaClientManagerListener.class.getName(), newFixedThreadPool(1));
        notifyBus.register(new Object() {
            @Subscribe
            public void notifyListeners(String kafkaName) {
                for (val listener : listeners) {
                    try {
                        listener.configuredKafkaClient(kafkaName);
                    } catch (Exception t) {
                        Reporter.error("listener error:", t);
                    }
                }
            }
        });
    }

    public static KafkaBatchClient getKafkaClient(String kafkaName) {
        return notNullThen(kafkaName, kafkaClients::get);
    }

    public static void putExternalKafkaClient(String kafkaName, @Nullable KafkaProducer<String, String> producer) {
        putExternalKafkaClient(kafkaName, producer, null);
    }

    public static void putExternalKafkaClient(String kafkaName, @Nullable KafkaProducer<String, String> producer,
                                              @Nullable BatchExecutorConfig batchConfig) {
        notNullThenRun(kafkaName, name -> eventBus.post(
                new ExternalKafkaClient(name, producer, nullThen(batchConfig, BatchExecutorConfig::new))));
    }

    public static void configKafkaClient(String kafkaName) {
        eventBus.post(kafkaName);
    }

    public static void addListener(KafkaClientManagerListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(KafkaClientManagerListener listener) {
        listeners.remove(listener);
    }

    @AllArgsConstructor
    @Getter
    private static class ExternalKafkaClient {

        private String kafkaName;
        private KafkaProducer<String, String> producer;
        private BatchExecutorConfig batchConfig;
    }
}
