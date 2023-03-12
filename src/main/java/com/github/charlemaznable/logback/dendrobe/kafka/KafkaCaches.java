package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.logback.dendrobe.KafkaLogBean;
import com.github.charlemaznable.logback.dendrobe.KafkaLogTopic;
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
public final class KafkaCaches {

    /**
     * 缓存 - 参数类型是否添加{@link KafkaLogBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class KafkaLogBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = simpleCache(from(KafkaLogBeanPresentCache::loadCache));

        static boolean isKafkaLogBeanPresent(Class<?> clazz) {
            return getUnchecked(cache, clazz);
        }

        @Nonnull
        static Boolean loadCache(@Nonnull Class<?> clazz) {
            return clazz.isAnnotationPresent(KafkaLogBean.class);
        }
    }

    /**
     * 缓存 - {@link KafkaLogBean}注解配置的KafkaName
     */
    @NoArgsConstructor(access = PRIVATE)
    static class KafkaLogBeanKafkaNameCache {

        static LoadingCache<Class<?>, String> cache
                = simpleCache(from(KafkaLogBeanKafkaNameCache::loadCache));

        static String getKafkaName(Class<?> clazz, String defaultKafkaName) {
            val configKafkaName = getUnchecked(cache, clazz);
            val kafkaName = defaultIfBlank(configKafkaName, defaultKafkaName);
            return defaultIfBlank(kafkaName, null);
        }

        @Nonnull
        static String loadCache(@Nonnull Class<?> clazz) {
            return checkNotNull(clazz.getAnnotation(KafkaLogBean.class)).value();
        }
    }

    /**
     * 缓存 - {@link KafkaLogTopic}注解配置的KafkaTopic
     */
    @NoArgsConstructor(access = PRIVATE)
    static class KafkaLogTopicCache {

        static LoadingCache<Class<?>, Optional<KafkaLogTopic>> cache
                = simpleCache(from(KafkaLogTopicCache::loadCache));

        static String getKafkaTopic(Class<?> clazz, String defaultKafkaTopic) {
            val kafkaLogTopicOptional = getUnchecked(cache, clazz);
            val kafkaTopic = kafkaLogTopicOptional
                    .map(index -> defaultIfBlank(index.value(), defaultKafkaTopic))
                    .orElse(defaultKafkaTopic);
            return defaultIfBlank(kafkaTopic, null);
        }

        @Nonnull
        static Optional<KafkaLogTopic> loadCache(@Nonnull Class<?> clazz) {
            return ofNullable(clazz.getAnnotation(KafkaLogTopic.class));
        }
    }
}
