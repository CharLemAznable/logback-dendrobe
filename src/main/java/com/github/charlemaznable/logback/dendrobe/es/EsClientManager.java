package com.github.charlemaznable.logback.dendrobe.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.slf4j.helpers.Util;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.logback.dendrobe.es.EsBatchClient.closeClient;
import static com.github.charlemaznable.logback.dendrobe.es.EsBatchClient.startClient;
import static com.github.charlemaznable.logback.dendrobe.es.EsBatchClient.stopClient;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class EsClientManager {

    private static final Map<String, EsBatchClient> esClients = new ConcurrentHashMap<>();
    private static final Map<String, String> esConfigs = new ConcurrentHashMap<>();
    private static final AsyncEventBus eventBus;
    private static final CopyOnWriteArrayList<EsClientManagerListener> listeners = new CopyOnWriteArrayList<>();
    private static final AsyncEventBus notifyBus;

    static {
        eventBus = new AsyncEventBus(EsClientManager.class.getName(), newFixedThreadPool(1));
        eventBus.register(new Object() {
            @Subscribe
            public void configEsClient(String esName) {
                // 内部配置的EsClient
                val configValue = EsConfigServiceElf
                        .configService().getEsConfigValue(esName);
                if (isNull(configValue)) {
                    // 不存在对应的配置
                    // 移除配置缓存
                    val previousConfig = esConfigs.remove(esName);
                    // 缓存不存在, 表示之前的EsClient不存在/为外部导入的EsClient, 则直接返回
                    if (isNull(previousConfig)) return;
                    // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                    // 则此处需要移除此内部配置的EsClient
                    // 移除内部配置的EsClient实例并关闭
                    closeClient(stopClient(esClients.remove(esName)));
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configValue.equals(esConfigs.get(esName))) return;
                // 校验配置
                val esConfig = EsConfigServiceElf.configService()
                        .parseEsConfig(esName, configValue);
                val batchConfig = EsConfigServiceElf.configService()
                        .parseBatchExecutorConfig(esName, configValue);
                // 不论之前的EsClient是内部配置的还是外部导入的
                // 此处都需要以当前的内部配置EsClient覆盖之
                // 保存配置缓存
                val previousConfig = esConfigs.put(esName, configValue);
                // 移除之前的EsClient实例
                val previous = stopClient(esClients.remove(esName));
                // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                // 则此处需要同步关闭, 对外部导入的EsClient不做操作
                if (nonNull(previousConfig)) closeClient(previous);
                // 同步新建EsClient实例并加入
                esClients.put(esName, startClient(esConfig, batchConfig));
                notifyBus.post(esName);
            }

            @Subscribe
            public void configEsClient(ExternalEsClient externalEsClient) {
                val esName = externalEsClient.getEsName();
                // 不论之前的EsClient是内部配置的还是外部导入的
                // 此处都需要以当前的外部导入EsClient覆盖之
                // 清除同名配置缓存
                val previousConfig = esConfigs.remove(esName);
                // 移除之前的EsClient实例
                val previous = stopClient(esClients.remove(esName));
                // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                // 则此处需要同步关闭, 对外部导入的EsClient不做操作
                if (nonNull(previousConfig)) closeClient(previous);
                // 加入新的EsClient实例
                val esClient = externalEsClient.getEsClient();
                val batchConfig = externalEsClient.getBatchConfig();
                if (nonNull(esClient)) {
                    esClients.put(esName, startClient(esClient, batchConfig));
                    notifyBus.post(esName);
                }
            }
        });

        notifyBus = new AsyncEventBus(EsClientManagerListener.class.getName(), newFixedThreadPool(1));
        notifyBus.register(new Object() {
            @Subscribe
            public void notifyListeners(String esName) {
                for (val listener : listeners) {
                    try {
                        listener.configuredEsClient(esName);
                    } catch (Exception t) {
                        Util.report("listener error:", t);
                    }
                }
            }
        });
    }

    public static EsBatchClient getEsClient(String esName) {
        return notNullThen(esName, esClients::get);
    }

    public static void putExternalEsClient(String esName, @Nullable ElasticsearchAsyncClient esClient) {
        putExternalEsClient(esName, esClient, null);
    }

    public static void putExternalEsClient(String esName, @Nullable ElasticsearchAsyncClient esClient,
                                           @Nullable BatchExecutorConfig batchConfig) {
        notNullThenRun(esName, name -> eventBus.post(
                new ExternalEsClient(name, esClient, nullThen(batchConfig, BatchExecutorConfig::new))));
    }

    public static void configEsClient(String esName) {
        eventBus.post(esName);
    }

    public static void addListener(EsClientManagerListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(EsClientManagerListener listener) {
        listeners.remove(listener);
    }

    @AllArgsConstructor
    @Getter
    private static class ExternalEsClient {

        private String esName;
        private ElasticsearchAsyncClient esClient;
        private BatchExecutorConfig batchConfig;
    }
}
