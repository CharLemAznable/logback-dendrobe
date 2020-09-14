package com.github.charlemaznable.logback.miner.appender;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.n3r.diamond.client.Miner;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.logback.miner.appender.VertxElf.buildVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertxQuietly;
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
                // 内部配置的vertx
                val configStone = new Miner().getStone(
                        VERTX_CONFIG_GROUP_NAME, vertxName);
                if (isNull(configStone)) {
                    // 不存在对应的diamond配置
                    // 移除配置缓存
                    val previousConfig = vertxConfigs.remove(vertxName);
                    // 缓存不存在, 表示之前的vertx不存在/为外部导入的vertx, 则直接返回
                    if (isNull(previousConfig)) return;
                    // 缓存存在, 表示之前的vertx为内部配置的vertx
                    // 则此处需要移除此内部配置的vertx
                    // 移除内部配置的vertx实例并关闭
                    closeVertxQuietly(vertxs.remove(vertxName));
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configStone.equals(vertxConfigs.get(vertxName))) return;
                // 校验配置
                val vertxOptions = parseStoneToVertxOptions(configStone);
                // 不论之前的vertx是内部配置的还是外部导入的
                // 此处都需要以当前的内部配置vertx覆盖之
                // 保存配置缓存
                val previousConfig = vertxConfigs.put(vertxName, configStone);
                // 移除之前的vertx实例
                val previous = vertxs.remove(vertxName);
                // 缓存存在, 表示之前的vertx为内部配置的vertx
                // 则此处需要同步关闭, 对外部导入的vertx不做操作
                if (nonNull(previousConfig)) closeVertx(previous);
                // 同步新建vertx实例并加入
                vertxs.put(vertxName, buildVertx(vertxOptions));
            }

            @Subscribe
            public void configVertx(ExternalVertx externalVertx) {
                val vertxName = externalVertx.getVertxName();
                // 不论之前的vertx是内部配置的还是外部导入的
                // 此处都需要以当前的外部导入vertx覆盖之
                // 清除同名配置缓存
                val previousConfig = vertxConfigs.remove(vertxName);
                // 移除之前的vertx实例
                val previous = vertxs.remove(vertxName);
                // 缓存存在, 表示之前的vertx为内部配置的vertx
                // 则此处需要同步关闭, 对外部导入的vertx不做操作
                if (nonNull(previousConfig)) closeVertx(previous);
                // 加入新的vertx实例
                val vertx = externalVertx.getVertx();
                if (nonNull(vertx)) vertxs.put(vertxName, vertx);
            }
        });
    }

    public static Vertx getVertx(String vertxName) {
        if (isNull(vertxName)) return null;
        return vertxs.get(vertxName);
    }

    public static void putExternalVertx(String vertxName, @Nullable Vertx vertx) {
        if (isNull(vertxName)) return;
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
