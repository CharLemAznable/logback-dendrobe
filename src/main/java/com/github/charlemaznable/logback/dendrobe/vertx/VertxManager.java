package com.github.charlemaznable.logback.dendrobe.vertx;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.slf4j.helpers.Reporter;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.vertx.VertxElf.buildVertx;
import static com.github.charlemaznable.core.vertx.VertxElf.closeVertx;
import static com.github.charlemaznable.core.vertx.VertxElf.closeVertxImmediately;
import static com.github.charlemaznable.logback.dendrobe.vertx.VertxOptionsServiceElf.optionsService;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VertxManager {

    private static final Map<String, Vertx> vertxs = new ConcurrentHashMap<>();
    private static final Map<String, String> vertxConfigs = new ConcurrentHashMap<>();
    private static final AsyncEventBus eventBus;
    private static final CopyOnWriteArrayList<VertxManagerListener> listeners = new CopyOnWriteArrayList<>();
    private static final AsyncEventBus notifyBus;

    static {
        eventBus = new AsyncEventBus(VertxManager.class.getName(), newFixedThreadPool(1));
        eventBus.register(new Object() {
            @Subscribe
            public void configVertx(String vertxName) {
                // 内部配置的vertx
                val configValue = optionsService().getVertxOptionsValue(vertxName);
                if (isNull(configValue)) {
                    // 不存在对应的配置
                    // 移除配置缓存
                    val previousConfig = vertxConfigs.remove(vertxName);
                    // 缓存不存在, 表示之前的vertx不存在/为外部导入的vertx, 则直接返回
                    if (isNull(previousConfig)) return;
                    // 缓存存在, 表示之前的vertx为内部配置的vertx
                    // 则此处需要移除此内部配置的vertx
                    // 移除内部配置的vertx实例并关闭
                    closeVertxImmediately(vertxs.remove(vertxName));
                    return;
                }

                // 配置未更改 -> 直接返回
                if (configValue.equals(vertxConfigs.get(vertxName))) return;
                // 校验配置
                val vertxOptions = optionsService().parseVertxOptions(vertxName, configValue);
                // 不论之前的vertx是内部配置的还是外部导入的
                // 此处都需要以当前的内部配置vertx覆盖之
                // 保存配置缓存
                val previousConfig = vertxConfigs.put(vertxName, configValue);
                // 移除之前的vertx实例
                val previous = vertxs.remove(vertxName);
                // 缓存存在, 表示之前的vertx为内部配置的vertx
                // 则此处需要同步关闭, 对外部导入的vertx不做操作
                if (nonNull(previousConfig)) closeVertxWithReport(previous);
                // 同步新建vertx实例并加入
                vertxs.put(vertxName, buildVertxWithReport(vertxOptions));
                notifyBus.post(vertxName);
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
                if (nonNull(previousConfig)) closeVertxWithReport(previous);
                // 加入新的vertx实例
                val vertx = externalVertx.getVertx();
                if (nonNull(vertx)) {
                    vertxs.put(vertxName, vertx);
                    notifyBus.post(vertxName);
                }
            }
        });

        notifyBus = new AsyncEventBus(VertxManagerListener.class.getName(), newFixedThreadPool(1));
        notifyBus.register(new Object() {
            @Subscribe
            public void notifyListeners(String vertxName) {
                for (val listener : listeners) {
                    try {
                        listener.configuredVertx(vertxName);
                    } catch (Exception t) {
                        Reporter.error("listener error:", t);
                    }
                }
            }
        });
    }

    public static Vertx getVertx(String vertxName) {
        return notNullThen(vertxName, vertxs::get);
    }

    public static void putExternalVertx(String vertxName, @Nullable Vertx vertx) {
        notNullThenRun(vertxName, name -> eventBus.post(new ExternalVertx(name, vertx)));
    }

    public static void configVertx(String vertxName) {
        eventBus.post(vertxName);
    }

    public static void addListener(VertxManagerListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(VertxManagerListener listener) {
        listeners.remove(listener);
    }

    private static void closeVertxWithReport(Vertx vertx) {
        closeVertx(vertx, reportFn("Closing Vertx failed"));
    }

    private static Vertx buildVertxWithReport(VertxOptions vertxOptions) {
        return buildVertx(vertxOptions, reportFn("Building Vertx failed"));
    }

    private static <T> Function<Throwable, T> reportFn(String message) {
        return t -> {
            Reporter.error(message, t);
            return null;
        };
    }

    @AllArgsConstructor
    @Getter
    private static class ExternalVertx {

        private String vertxName;
        private Vertx vertx;
    }
}
