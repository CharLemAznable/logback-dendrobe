package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.core.Context;
import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import com.github.charlemaznable.logback.dendrobe.EqlLogColumn;
import com.github.charlemaznable.logback.dendrobe.EqlLogRollingSql;
import com.github.charlemaznable.logback.dendrobe.EqlLogSkip;
import com.github.charlemaznable.logback.dendrobe.EqlLogSql;
import com.github.charlemaznable.logback.dendrobe.EqlLogTable;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.Eql;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.getUnchecked;
import static com.github.charlemaznable.core.lang.LoadingCachee.manualCache;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlConfigServiceElf.configService;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlTableNameRolling.ACTIVE_TABLE_NAME;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.n3r.eql.util.Names.convertCamelToUnderscore;

@NoArgsConstructor(access = PRIVATE)
final class EqlCaches {

    static void useSqlFile(Eql eql, String sqlFile, Class<?> sqlClass, Class<?> clazz) {
        if (isNotBlank(sqlFile)) {
            eql.useSqlFile(sqlFile);
        } else if (Void.class != sqlClass) {
            eql.useSqlFile(sqlClass);
        } else {
            eql.useSqlFile(clazz);
        }
    }

    /**
     * 缓存 - 参数类型是否添加{@link EqlLogBean}注解
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogBeanPresentCache {

        static LoadingCache<Class<?>, Boolean> cache
                = simpleCache(from(EqlLogBeanPresentCache::loadCache));

        static boolean isEqlLogBeanPresent(Class<?> clazz) {
            return getUnchecked(cache, clazz);
        }

        @Nonnull
        static Boolean loadCache(@Nonnull Class<?> clazz) {
            return clazz.isAnnotationPresent(EqlLogBean.class);
        }
    }

    /**
     * 缓存 - 参数类型入库日志使用的{@link Eql}对象
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogBeanEqlCache {

        static LoadingCache<Class<?>, String> cache
                = simpleCache(from(EqlLogBeanEqlCache::loadCache));

        static Eql getEqlLogBeanEql(String defaultConnection) {
            if (isBlank(defaultConnection)) return null;
            val configValue = configService().getEqlConfigValue(defaultConnection);
            val eqlConfig = configService().parseEqlConfig(defaultConnection, configValue);
            if (isNull(eqlConfig)) return null;
            return new Eql(eqlConfig);
        }

        static Eql getEqlLogBeanEql(Class<?> clazz, String defaultConnection) {
            val configConnection = getUnchecked(cache, clazz);
            val connectionName = defaultIfBlank(configConnection, defaultConnection);
            if (isBlank(connectionName)) return null;
            val configValue = configService().getEqlConfigValue(connectionName);
            val eqlConfig = configService().parseEqlConfig(connectionName, configValue);
            if (isNull(eqlConfig)) return null;
            return new Eql(eqlConfig);
        }

        @Nonnull
        static String loadCache(@Nonnull Class<?> clazz) {
            return checkNotNull(clazz.getAnnotation(EqlLogBean.class)).value();
        }
    }

    /**
     * 缓存 - 处理参数类型入库日志使用的{@link Eql#useSqlFile(String)}
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogSqlCache {

        static LoadingCache<Class<?>, Optional<EqlLogSql>> cache
                = simpleCache(from(EqlLogSqlCache::loadCache));

        static boolean useEqlLogSql(Class<?> clazz, Eql eql) {
            val eqlLogSqlOptional = getUnchecked(cache, clazz);
            if (!eqlLogSqlOptional.isPresent()) return false;

            val eqlLogSql = eqlLogSqlOptional.get();
            useSqlFile(eql, eqlLogSql.sqlFile(),
                    eqlLogSql.sqlClass(), clazz);

            eql.id(defaultIfBlank(eqlLogSql.sqlId(),
                    "log" + clazz.getSimpleName()));
            return true;
        }

        @Nonnull
        static Optional<EqlLogSql> loadCache(@Nonnull Class<?> clazz) {
            return ofNullable(clazz.getAnnotation(EqlLogSql.class));
        }
    }

    /**
     * 缓存 - 参数类型默认PojoSql
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogPojoSqlCache {

        static LoadingCache<Class<?>, String> cache
                = simpleCache(from(EqlLogPojoSqlCache::loadCache));

        static String getEqlLogPojoSql(Class<?> clazz) {
            return getUnchecked(cache, clazz);
        }

        @Nonnull
        static String loadCache(@Nonnull Class<?> clazz) {
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
            val eqlLogRollingSql = clazz.getAnnotation(EqlLogRollingSql.class);
            if (nonNull(eqlLogRollingSql)) return "$" + ACTIVE_TABLE_NAME + "$";

            val eqlLogTable = clazz.getAnnotation(EqlLogTable.class);
            return checkNull(eqlLogTable, () ->
                    convertCamelToUnderscore(clazz.getSimpleName()), EqlLogTable::value);
        }

        private static List<Field> parsePojoFields(Class<?> clazz) {
            val declaredFields = clazz.getDeclaredFields();
            val pojoFields = new ArrayList<Field>();
            for (val field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers()) ||
                        field.isAnnotationPresent(EqlLogSkip.class)) continue;

                pojoFields.add(field);
            }
            return pojoFields;
        }

        private static String parseColumnName(Field field) {
            val eqlLogColumn = field.getAnnotation(EqlLogColumn.class);
            return checkNull(eqlLogColumn, () ->
                    convertCamelToUnderscore(field.getName()), EqlLogColumn::value);
        }
    }

    /**
     * 缓存 - 表名滚动配置缓存
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogTableNameRollingCache {

        static EqlTableNameRolling nullTableNameRolling = new EqlTableNameRolling(null, null);

        static Cache<String, EqlTableNameRolling> cache = manualCache();

        @SneakyThrows
        static EqlTableNameRolling getTableNameRolling(String tableNamePatternStr, Context context) {
            return checkNull(tableNamePatternStr, () -> nullTableNameRolling, k ->
                    get(cache, k, () -> new EqlTableNameRolling(k, context)));
        }
    }

    /**
     * 缓存 - 表名滚动注解配置缓存
     */
    @NoArgsConstructor(access = PRIVATE)
    static class EqlLogRollingSqlCache {

        static LoadingCache<Class<?>, Optional<EqlLogRollingSql>> cache
                = simpleCache(from(EqlLogRollingSqlCache::loadCache));

        static String getTableNamePattern(Class<?> clazz) {
            val eqlLogRollingSqlOptional = getUnchecked(cache, clazz);
            if (!eqlLogRollingSqlOptional.isPresent()) return null;

            val eqlLogRollingSql = eqlLogRollingSqlOptional.get();
            return eqlLogRollingSql.tableNamePattern();
        }

        static boolean useEqlLogRollingSql(Class<?> clazz, Eql eql) {
            val eqlLogRollingSqlOptional = getUnchecked(cache, clazz);
            if (!eqlLogRollingSqlOptional.isPresent()) return false;

            val eqlLogRollingSql = eqlLogRollingSqlOptional.get();
            useSqlFile(eql, eqlLogRollingSql.sqlFile(),
                    eqlLogRollingSql.sqlClass(), clazz);

            eql.id(defaultIfBlank(eqlLogRollingSql.sqlId(),
                    "prepare" + clazz.getSimpleName()));
            return true;
        }

        @Nonnull
        static Optional<EqlLogRollingSql> loadCache(@Nonnull Class<?> clazz) {
            return ofNullable(clazz.getAnnotation(EqlLogRollingSql.class));
        }
    }
}
