package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackColumn;
import com.github.charlemaznable.logback.miner.annotation.LogbackSkip;
import com.github.charlemaznable.logback.miner.annotation.LogbackSql;
import com.github.charlemaznable.logback.miner.annotation.LogbackTable;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.Eql;
import org.n3r.eql.diamond.Dql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.n3r.eql.config.EqlDiamondConfig.EQL_CONFIG_GROUP_NAME;
import static org.n3r.eql.util.Names.convertCamelToUnderscore;

@NoArgsConstructor(access = PRIVATE)
class DqlCaches {

    /**
     * 缓存 - 参数类型是否添加{@link LogbackBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class LogbackBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = newBuilder().build(CacheLoader.from(LogbackBeanPresentCache::loadCache));

        static boolean isLogbackBeanPresent(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static Boolean loadCache(Class<?> clazz) {
            return clazz.isAnnotationPresent(LogbackBean.class);
        }
    }

    /**
     * 缓存 - 参数类型入库日志使用的{@link Eql}对象
     */
    @NoArgsConstructor(access = PRIVATE)
    static class LogbackBeanDqlCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(LogbackBeanDqlCache::loadCache));

        static Dql getLogbackBeanDql(Class<?> clazz, String defaultConnection) {
            var configConnection = cache.getUnchecked(clazz);
            var connectionName = defaultIfBlank(configConnection, defaultConnection);
            if (isBlank(connectionName)) return null;

            var properties = new Miner().getProperties(EQL_CONFIG_GROUP_NAME, connectionName);
            if (properties.isEmpty()) return null;
            return new Dql(connectionName);
        }

        static String loadCache(Class<?> clazz) {
            return requireNonNull(clazz.getAnnotation(LogbackBean.class)).value();
        }
    }

    /**
     * 缓存 - 处理参数类型入库日志使用的{@link Eql#useSqlFile(String)}
     */
    @NoArgsConstructor(access = PRIVATE)
    static class LogbackSqlCache {

        static LoadingCache<Class<?>, Optional<LogbackSql>> cache
                = newBuilder().build(CacheLoader.from(LogbackSqlCache::loadCache));

        static boolean useLogbackSql(Class<?> clazz, Eql dql) {
            var logbackSqlOptional = cache.getUnchecked(clazz);
            if (!logbackSqlOptional.isPresent()) return false;

            var logbackSql = logbackSqlOptional.get();
            if (isNotBlank(logbackSql.sqlFile())) {
                dql.useSqlFile(logbackSql.sqlFile());
            } else if (Void.class != logbackSql.sqlClass()) {
                dql.useSqlFile(logbackSql.sqlClass());
            } else {
                dql.useSqlFile(clazz);
            }

            dql.id(defaultIfBlank(logbackSql.sqlId(), "log" + clazz.getSimpleName()));
            return true;
        }

        static Optional<LogbackSql> loadCache(Class<?> clazz) {
            return Optional.ofNullable(clazz.getAnnotation(LogbackSql.class));
        }
    }

    /**
     * 缓存 - 参数类型默认PojoSql
     */
    @NoArgsConstructor(access = PRIVATE)
    static class LogbackPojoSqlCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(LogbackPojoSqlCache::loadCache));

        static String getLogbackPojoSql(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static String loadCache(Class<?> clazz) {
            var tableName = parseTableName(clazz);
            var insertSql = new StringBuilder("insert into ").append(tableName).append("(");
            var valuesSql = new StringBuilder(") values(");

            for (var field : parsePojoFields(clazz)) {
                var columnName = parseColumnName(field);
                insertSql.append(columnName).append(',');
                valuesSql.append("#arg.").append(field.getName()).append("#,");
            }

            var c = insertSql.charAt(insertSql.length() - 1);
            if (c != ',') throw new IllegalArgumentException(
                    "there is no property to save for class " + clazz);

            insertSql.delete(insertSql.length() - 1, insertSql.length());
            valuesSql.delete(valuesSql.length() - 1, valuesSql.length());
            insertSql.append(valuesSql).append(')');
            return insertSql.toString();
        }

        private static String parseTableName(Class<?> clazz) {
            var logbackTable = clazz.getAnnotation(LogbackTable.class);
            return nonNull(logbackTable) ? logbackTable.value()
                    : convertCamelToUnderscore(clazz.getSimpleName());
        }

        private static List<Field> parsePojoFields(Class<?> clazz) {
            var declaredFields = clazz.getDeclaredFields();
            var pojoFields = new ArrayList<Field>();
            for (var field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers()) ||
                        field.isAnnotationPresent(LogbackSkip.class)) continue;

                pojoFields.add(field);
            }
            return pojoFields;
        }

        private static String parseColumnName(Field field) {
            var logbackColumn = field.getAnnotation(LogbackColumn.class);
            return nonNull(logbackColumn) ? logbackColumn.value()
                    : convertCamelToUnderscore(field.getName());
        }
    }
}
