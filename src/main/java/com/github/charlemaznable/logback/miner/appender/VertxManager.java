package com.github.charlemaznable.logback.miner.appender;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.n3r.diamond.client.Miner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.logback.miner.appender.VertxElf.buildVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.parseStoneToVertxOptions;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;

public final class VertxManager {

    public static final String VERTX_CONFIG_GROUP_NAME = "VertxConfig";
    private static final Map<String, Vertx> vertxs = new ConcurrentHashMap<>();
    private static final Map<String, String> vertxConfigs = new ConcurrentHashMap<>();
    private static final AsyncEventBus eventBus;

    static {
        eventBus = new AsyncEventBus(VertxManager.class.getName(), newFixedThreadPool(1));
        eventBus.register(new Object() {
            @Subscribe
            public void configVertx(String vertxName) {
                val configStone = new Miner().getStone(
                        VERTX_CONFIG_GROUP_NAME, vertxName);
                if (isNull(configStone)) {
                    // 不存在对应的diamond配置 -> 移除配置缓存, 移除vertx实例并关闭
                    vertxConfigs.remove(vertxName);
                    val previous = vertxs.remove(vertxName);
                    if (nonNull(previous)) previous.close();
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configStone.equals(vertxConfigs.get(vertxName))) return;
                // 校验配置
                val vertxOptions = parseStoneToVertxOptions(configStone);
                // 保存配置缓存
                vertxConfigs.put(vertxName, configStone);
                // 移除之前的vertx实例并同步关闭
                val previous = vertxs.remove(vertxName);
                if (nonNull(previous)) closeVertx(previous);
                // 同步新建vertx实例并加入
                vertxs.put(vertxName, buildVertx(vertxOptions));
            }

            @Subscribe
            public void configVertx(ExternalVertx externalVertx) {
                val vertxName = externalVertx.getVertxName();
                // 清除同名配置缓存
                vertxConfigs.put(vertxName, null);
                // 移除之前的vertx实例并同步关闭
                val previous = vertxs.remove(vertxName);
                if (nonNull(previous)) closeVertx(previous);
                // 加入新的vertx实例
                vertxs.put(vertxName, externalVertx.getVertx());
            }
        });
    }

    public static Vertx getVertx(String vertxName) {
        if (isNull(vertxName)) return null;
        return vertxs.get(vertxName);
    }

    public static void putExternalVertx(String vertxName, Vertx vertx) {
        if (isNull(vertxName) || isNull(vertx)) return;
        eventBus.post(new ExternalVertx(vertxName, vertx));
    }

    public static void configVertx(String vertxName) {
        eventBus.post(vertxName);
    }

    private VertxManager() {}

    @AllArgsConstructor
    @Getter
    private static class ExternalVertx {

        private String vertxName;
        private Vertx vertx;
    }
}
