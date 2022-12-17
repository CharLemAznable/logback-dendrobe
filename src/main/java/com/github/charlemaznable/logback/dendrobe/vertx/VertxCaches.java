package com.github.charlemaznable.logback.dendrobe.vertx;

import com.github.charlemaznable.logback.dendrobe.VertxLogAddress;
import com.github.charlemaznable.logback.dendrobe.VertxLogBean;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.getUnchecked;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Optional.ofNullable;
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
                = simpleCache(from(VertxLogBeanPresentCache::loadCache));

        static boolean isVertxLogBeanPresent(Class<?> clazz) {
            return getUnchecked(cache, clazz);
        }

        @Nonnull
        static Boolean loadCache(@Nonnull Class<?> clazz) {
            return clazz.isAnnotationPresent(VertxLogBean.class);
        }
    }

    /**
     * 缓存 - {@link VertxLogBean}注解配置的VertxName
     */
    @NoArgsConstructor(access = PRIVATE)
    static class VertxLogBeanVertxNameCache {

        static LoadingCache<Class<?>, String> cache
                = simpleCache(from(VertxLogBeanVertxNameCache::loadCache));

        static String getVertxName(Class<?> clazz, String defaultVertxName) {
            val configVertxName = getUnchecked(cache, clazz);
            val vertxName = defaultIfBlank(configVertxName, defaultVertxName);
            return defaultIfBlank(vertxName, null);
        }

        @Nonnull
        static String loadCache(@Nonnull Class<?> clazz) {
            return checkNotNull(clazz.getAnnotation(VertxLogBean.class)).value();
        }
    }

    /**
     * 缓存 - {@link VertxLogAddress}注解配置的VertxAddress
     */
    @NoArgsConstructor(access = PRIVATE)
    static class VertxLogAddressCache {

        static LoadingCache<Class<?>, Optional<VertxLogAddress>> cache
                = simpleCache(from(VertxLogAddressCache::loadCache));

        static String getVertxAddress(Class<?> clazz, String defaultAddress) {
            val vertxLogAddressOptional = getUnchecked(cache, clazz);
            val vertxAddress = vertxLogAddressOptional
                    .map(address -> defaultIfBlank(address.value(), defaultAddress))
                    .orElse(defaultAddress);
            return defaultIfBlank(vertxAddress, null);
        }

        @Nonnull
        static Optional<VertxLogAddress> loadCache(@Nonnull Class<?> clazz) {
            return ofNullable(clazz.getAnnotation(VertxLogAddress.class));
        }
    }
}
