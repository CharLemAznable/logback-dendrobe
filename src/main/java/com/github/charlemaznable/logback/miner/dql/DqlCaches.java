package com.github.charlemaznable.logback.miner.dql;

import ch.qos.logback.core.Context;
import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import com.github.charlemaznable.logback.miner.annotation.DqlLogColumn;
import com.github.charlemaznable.logback.miner.annotation.DqlLogRollingSql;
import com.github.charlemaznable.logback.miner.annotation.DqlLogSkip;
import com.github.charlemaznable.logback.miner.annotation.DqlLogSql;
import com.github.charlemaznable.logback.miner.annotation.DqlLogTable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.diamond.Dql;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.charlemaznable.logback.miner.dql.DqlTableNameRolling.ACTIVE_TABLE_NAME;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.n3r.eql.config.EqlDiamondConfig.EQL_CONFIG_GROUP_NAME;
import static org.n3r.eql.util.Names.convertCamelToUnderscore;

@NoArgsConstructor(access = PRIVATE)
final class DqlCaches {

    /**
     * 缓存 - 参数类型是否添加{@link DqlLogBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = newBuilder().build(CacheLoader.from(DqlLogBeanPresentCache::loadCache));

        static boolean isDqlLogBeanPresent(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static Boolean loadCache(Class<?> clazz) {
            return clazz.isAnnotationPresent(DqlLogBean.class);
        }
    }

    /**
     * 缓存 - 参数类型入库日志使用的{@link Dql}对象
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogBeanDqlCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(DqlLogBeanDqlCache::loadCache));

        static Dql getDqlLogBeanDql(String defaultConnection) {
            if (isBlank(defaultConnection)) return null;

            val properties = new Miner().getProperties(EQL_CONFIG_GROUP_NAME, defaultConnection);
            if (properties.isEmpty()) return null;
            return new Dql(defaultConnection);
        }

        static Dql getDqlLogBeanDql(Class<?> clazz, String defaultConnection) {
            val configConnection = cache.getUnchecked(clazz);
            val connectionName = defaultIfBlank(configConnection, defaultConnection);
            if (isBlank(connectionName)) return null;

            val properties = new Miner().getProperties(EQL_CONFIG_GROUP_NAME, connectionName);
            if (properties.isEmpty()) return null;
            return new Dql(connectionName);
        }

        @Nonnull
        static String loadCache(Class<?> clazz) {
            return requireNonNull(clazz.getAnnotation(DqlLogBean.class)).value();
        }
    }

    /**
     * 缓存 - 处理参数类型入库日志使用的{@link Dql#useSqlFile(String)}
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogSqlCache {

        static LoadingCache<Class<?>, Optional<DqlLogSql>> cache
                = newBuilder().build(CacheLoader.from(DqlLogSqlCache::loadCache));

        static boolean useDqlLogSql(Class<?> clazz, Dql dql) {
            val dqlLogSqlOptional = cache.getUnchecked(clazz);
            if (!dqlLogSqlOptional.isPresent()) return false;

            val dqlLogSql = dqlLogSqlOptional.get();
            useSqlFile(dql, dqlLogSql.sqlFile(),
                    dqlLogSql.sqlClass(), clazz);

            dql.id(defaultIfBlank(dqlLogSql.sqlId(),
                    "log" + clazz.getSimpleName()));
            return true;
        }

        @Nonnull
        static Optional<DqlLogSql> loadCache(Class<?> clazz) {
            return Optional.ofNullable(clazz.getAnnotation(DqlLogSql.class));
        }
    }

    /**
     * 缓存 - 参数类型默认PojoSql
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogPojoSqlCache {

        static LoadingCache<Class<?>, String> cache
                = newBuilder().build(CacheLoader.from(DqlLogPojoSqlCache::loadCache));

        static String getDqlLogPojoSql(Class<?> clazz) {
            return cache.getUnchecked(clazz);
        }

        static String loadCache(Class<?> clazz) {
            val tableName = parseTableName(clazz);
            val insertSql = new StringBuilder("insert into ").append(tableName).append("(");
            val valuesSql = new StringBuilder(") values(");

            for (val field : parsePojoFields(clazz)) {
                val columnName = parseColumnName(field);
                insertSql.append(columnName).append(',');
                valuesSql.append("#arg.").append(field.getName()).append("#,");
            }

            val c = insertSql.charAt(insertSql.length() - 1);
            if (c != ',') throw new IllegalArgumentException(
                    "there is no property to save for class " + clazz);

            insertSql.delete(insertSql.length() - 1, insertSql.length());
            valuesSql.delete(valuesSql.length() - 1, valuesSql.length());
            insertSql.append(valuesSql).append(')');
            return insertSql.toString();
        }

        private static String parseTableName(Class<?> clazz) {
            val dqlLogRollingSql = clazz.getAnnotation(DqlLogRollingSql.class);
            if (nonNull(dqlLogRollingSql)) return "$" + ACTIVE_TABLE_NAME + "$";

            val dqlLogTable = clazz.getAnnotation(DqlLogTable.class);
            return nonNull(dqlLogTable) ? dqlLogTable.value()
                    : convertCamelToUnderscore(clazz.getSimpleName());
        }

        private static List<Field> parsePojoFields(Class<?> clazz) {
            val declaredFields = clazz.getDeclaredFields();
            val pojoFields = new ArrayList<Field>();
            for (val field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers()) ||
                        field.isAnnotationPresent(DqlLogSkip.class)) continue;

                pojoFields.add(field);
            }
            return pojoFields;
        }

        private static String parseColumnName(Field field) {
            val dqlLogColumn = field.getAnnotation(DqlLogColumn.class);
            return nonNull(dqlLogColumn) ? dqlLogColumn.value()
                    : convertCamelToUnderscore(field.getName());
        }
    }

    /**
     * 缓存 - 表名滚动配置缓存
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogTableNameRollingCache {

        static DqlTableNameRolling nullTableNameRolling = new DqlTableNameRolling(null, null);

        static Cache<String, DqlTableNameRolling> cache = newBuilder().build();

        @SneakyThrows
        static DqlTableNameRolling getTableNameRolling(String tableNamePatternStr, Context context) {
            if (isNull(tableNamePatternStr)) return nullTableNameRolling;
            return cache.get(tableNamePatternStr, () ->
                    new DqlTableNameRolling(tableNamePatternStr, context));
        }
    }

    /**
     * 缓存 - 表名滚动注解配置缓存
     */
    @NoArgsConstructor(access = PRIVATE)
    static class DqlLogRollingSqlCache {

        static LoadingCache<Class<?>, Optional<DqlLogRollingSql>> cache
                = newBuilder().build(CacheLoader.from(DqlLogRollingSqlCache::loadCache));

        static String getTableNamePattern(Class<?> clazz) {
            val dqlLogRollingSqlOptional = cache.getUnchecked(clazz);
            if (!dqlLogRollingSqlOptional.isPresent()) return null;

            val dqlLogRollingSql = dqlLogRollingSqlOptional.get();
            return dqlLogRollingSql.tableNamePattern();
        }

        static boolean useDqlLogRollingSql(Class<?> clazz, Dql dql) {
            val dqlLogRollingSqlOptional = cache.getUnchecked(clazz);
            if (!dqlLogRollingSqlOptional.isPresent()) return false;

            val dqlLogRollingSql = dqlLogRollingSqlOptional.get();
            useSqlFile(dql, dqlLogRollingSql.sqlFile(),
                    dqlLogRollingSql.sqlClass(), clazz);

            dql.id(defaultIfBlank(dqlLogRollingSql.sqlId(),
                    "prepare" + clazz.getSimpleName()));
            return true;
        }

        @Nonnull
        static Optional<DqlLogRollingSql> loadCache(Class<?> clazz) {
            return Optional.ofNullable(clazz.getAnnotation(DqlLogRollingSql.class));
        }
    }

    static void useSqlFile(Dql dql, String sqlFile, Class<?> sqlClass, Class<?> clazz) {
        if (isNotBlank(sqlFile)) {
            dql.useSqlFile(sqlFile);
        } else if (Void.class != sqlClass) {
            dql.useSqlFile(sqlClass);
        } else {
            dql.useSqlFile(clazz);
        }
    }
}
