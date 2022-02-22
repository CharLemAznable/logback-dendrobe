package com.github.charlemaznable.logback.miner.es;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.es.diamond.EsClientElf.buildEsClient;
import static com.github.charlemaznable.es.diamond.EsClientElf.closeEsClient;
import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.getEsConfigStone;
import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.parseStoneToEsConfig;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class EsClientManager {

    private static final Map<String, RestHighLevelClient> esClients = new ConcurrentHashMap<>();
    private static final Map<String, String> esConfigs = new ConcurrentHashMap<>();
    private static final AsyncEventBus eventBus;

    static {
        eventBus = new AsyncEventBus(EsClientManager.class.getName(), newFixedThreadPool(1));
        eventBus.register(new Object() {
            @Subscribe
            public void configEsClient(String esName) {
                // 内部配置的EsClient
                val configStone = getEsConfigStone(esName);
                if (isNull(configStone)) {
                    // 不存在对应的diamond配置
                    // 移除配置缓存
                    val previousConfig = esConfigs.remove(esName);
                    // 缓存不存在, 表示之前的EsClient不存在/为外部导入的EsClient, 则直接返回
                    if (isNull(previousConfig)) return;
                    // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                    // 则此处需要移除此内部配置的EsClient
                    // 移除内部配置的EsClient实例并关闭
                    closeEsClient(esClients.remove(esName));
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configStone.equals(esConfigs.get(esName))) return;
                // 校验配置
                val esConfig = parseStoneToEsConfig(configStone);
                // 不论之前的EsClient是内部配置的还是外部导入的
                // 此处都需要以当前的内部配置EsClient覆盖之
                // 保存配置缓存
                val previousConfig = esConfigs.put(esName, configStone);
                // 移除之前的EsClient实例
                val previous = esClients.remove(esName);
                // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                // 则此处需要同步关闭, 对外部导入的EsClient不做操作
                if (nonNull(previousConfig)) closeEsClient(previous);
                // 同步新建EsClient实例并加入
                esClients.put(esName, buildEsClient(esConfig));
            }

            @Subscribe
            public void configEsClient(ExternalEsClient externalEsClient) {
                val esName = externalEsClient.getEsName();
                // 不论之前的EsClient是内部配置的还是外部导入的
                // 此处都需要以当前的外部导入EsClient覆盖之
                // 清除同名配置缓存
                val previousConfig = esConfigs.remove(esName);
                // 移除之前的EsClient实例
                val previous = esClients.remove(esName);
                // 缓存存在, 表示之前的EsClient为内部配置的EsClient
                // 则此处需要同步关闭, 对外部导入的EsClient不做操作
                if (nonNull(previousConfig)) closeEsClient(previous);
                // 加入新的EsClient实例
                val esClient = externalEsClient.getEsClient();
                if (nonNull(esClient)) esClients.put(esName, esClient);
            }
        });
    }

    public static RestHighLevelClient getEsClient(String esName) {
        if (isNull(esName)) return null;
        return esClients.get(esName);
    }

    public static void putExternalEsClient(String esName, @Nullable RestHighLevelClient esClient) {
        if (isNull(esName)) return;
        eventBus.post(new ExternalEsClient(esName, esClient));
    }

    public static void configEsClient(String esName) {
        eventBus.post(esName);
    }

    @AllArgsConstructor
    @Getter
    private static class ExternalEsClient {

        private String esName;
        private RestHighLevelClient esClient;
    }
}
