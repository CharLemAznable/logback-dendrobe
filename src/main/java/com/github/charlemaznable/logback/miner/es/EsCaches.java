package com.github.charlemaznable.logback.miner.es;

import com.github.charlemaznable.logback.miner.annotation.EsLogBean;
import com.github.charlemaznable.logback.miner.annotation.EsLogIndex;
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
public final class EsCaches {

    /**
     * 缓存 - 参数类型是否添加{@link EsLogBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EsLogBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = newBuilder().build(CacheLoader.from(EsLogBeanPresentCache::loadCache));

        static boolean isEsLogBeanPresent(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static Boolean loadCache(Class<?> clazz) {
            return clazz.isAnnotationPresent(EsLogBean.class);
        }
    }

    /**
     * 缓存 - {@link EsLogBean}注解配置的EsName
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EsLogBeanEsNameCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(EsLogBeanEsNameCache::loadCache));

        static String getEsName(Class<?> clazz, String defaultEsName) {
            val configEsName = cache.getUnchecked(clazz);
            val esName = defaultIfBlank(configEsName, defaultEsName);
            return defaultIfBlank(esName, null);
        }

        @Nonnull
        static String loadCache(Class<?> clazz) {
            return requireNonNull(clazz.getAnnotation(EsLogBean.class)).value();
        }
    }

    /**
     * 缓存 - {@link EsLogIndex}注解配置的EsIndex
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EsLogIndexCache {

        static LoadingCache<Class<?>, Optional<EsLogIndex>> cache
                = newBuilder().build(CacheLoader.from(EsLogIndexCache::loadCache));

        static String getEsIndex(Class<?> clazz, String defaultEsIndex) {
            val esLogIndexOptional = cache.getUnchecked(clazz);
            val esIndex = !esLogIndexOptional.isPresent() ? defaultEsIndex
                    : defaultIfBlank(esLogIndexOptional.get().value(), defaultEsIndex);
            return defaultIfBlank(esIndex, null);
        }

        @Nonnull
        static Optional<EsLogIndex> loadCache(Class<?> clazz) {
            return Optional.ofNullable(clazz.getAnnotation(EsLogIndex.class));
        }
    }
}
