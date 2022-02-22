package com.github.charlemaznable.logback.miner.vertx;

import com.github.charlemaznable.logback.miner.annotation.VertxLogAddress;
import com.github.charlemaznable.logback.miner.annotation.VertxLogBean;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@NoArgsConstructor(access = PRIVATE)
public final class VertxCaches {

    /**
     * 缓存 - 参数类型是否添加{@link VertxLogBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class VertxLogBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = newBuilder().build(CacheLoader.from(VertxLogBeanPresentCache::loadCache));

        static boolean isVertxLogBeanPresent(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static Boolean loadCache(Class<?> clazz) {
            return clazz.isAnnotationPresent(VertxLogBean.class);
        }
    }

    /**
     * 缓存 - {@link VertxLogBean}注解配置的VertxName
     */
    @NoArgsConstructor(access = PRIVATE)
    static class VertxLogBeanVertxNameCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(VertxLogBeanVertxNameCache::loadCache));

        static String getVertxName(Class<?> clazz, String defaultVertxName) {
            val configVertxName = cache.getUnchecked(clazz);
            val vertxName = defaultIfBlank(configVertxName, defaultVertxName);
            return defaultIfBlank(vertxName, null);
        }

        @Nonnull
        static String loadCache(Class<?> clazz) {
            return requireNonNull(clazz.getAnnotation(VertxLogBean.class)).value();
        }
    }

    /**
     * 缓存 - {@link VertxLogAddress}注解配置的VertxAddress
     */
    @NoArgsConstructor(access = PRIVATE)
    static class VertxLogAddressCache {

        static LoadingCache<Class<?>, Optional<VertxLogAddress>> cache
                = newBuilder().build(CacheLoader.from(VertxLogAddressCache::loadCache));

        static String getVertxAddress(Class<?> clazz, String defaultAddress) {
            val vertxLogAddressOptional = cache.getUnchecked(clazz);
            val vertxAddress = !vertxLogAddressOptional.isPresent() ? defaultAddress
                    : defaultIfBlank(vertxLogAddressOptional.get().value(), defaultAddress);
            return defaultIfBlank(vertxAddress, null);
        }

        @Nonnull
        static Optional<VertxLogAddress> loadCache(Class<?> clazz) {
            return Optional.ofNullable(clazz.getAnnotation(VertxLogAddress.class));
        }
    }
}
