package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.dendrobe.appender.AsyncAppender;
import com.github.charlemaznable.logback.dendrobe.effect.Effector;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;

import static com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf.buildEventMap;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogBeanEqlCache.getEqlLogBeanEql;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogBeanPresentCache.isEqlLogBeanPresent;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogPojoSqlCache.getEqlLogPojoSql;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogRollingSqlCache.getTableNamePattern;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogSqlCache.useEqlLogSql;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogTableNameRollingCache.getTableNameRolling;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlEffectorBuilder.EQL_EFFECTOR;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlTableNameRolling.ACTIVE_TABLE_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class EqlAppender extends AsyncAppender {

    public static final String DEFAULT_EQL_CONNECTION = "DEFAULT";

    private final InternalAppender appender;

    public EqlAppender() {
        this.appender = new InternalAppender();
        this.appender.setEqlConnection(DEFAULT_EQL_CONNECTION);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public EqlAppender setEqlConnection(String eqlConnection) {
        this.appender.setEqlConnection(eqlConnection);
        return this;
    }

    public EqlAppender setEqlSql(String eqlSql) {
        this.appender.setEqlSql(eqlSql);
        return this;
    }

    public EqlAppender setTableNamePattern(String tableNamePattern) {
        this.appender.setTableNamePatternStr(tableNamePattern);
        return this;
    }

    public EqlAppender setEqlPrepareSql(String eqlPrepareSql) {
        this.appender.setEqlPrepareSql(eqlPrepareSql);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured EqlAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getEffectorLevelInt(EQL_EFFECTOR) > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Setter
        private String eqlConnection;
        @Setter
        private String eqlSql;
        @Setter
        private String tableNamePatternStr;
        @Setter
        private String eqlPrepareSql;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            try {
                EqlExecuteWrapper.preExecute(eventObject);

                val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
                val arguments = Arrays.stream(argumentArray).parallel()
                        .filter(arg -> nonNull(arg) && isEqlLogBeanPresent(arg.getClass())).toList();
                // 日志不包含@EqlLogBean注解的参数, 执行默认连接的默认SQL
                if (arguments.isEmpty()) {
                    val eql = getEqlLogBeanEql(eqlConnection);
                    // 未指定默认连接或默认SQL, 则跳过
                    if (isNull(eql) || isBlank(eqlSql)) return;

                    val rolling = getTableNameRolling(
                            tableNamePatternStr, context);
                    rolling.rolling(eql, eqlPrepareSql);

                    // 公共参数, 包含event/mdc/ctx-property
                    val paramMap = buildEventMap(eventObject);
                    paramMap.put(ACTIVE_TABLE_NAME, rolling.getActiveTableName());
                    eql.params(paramMap).dynamics(paramMap).execute(eqlSql);
                    return;
                }

                // 遍历@EqlLogBean注解的参数
                for (val argument : arguments) {
                    val clazz = argument.getClass();
                    val eql = getEqlLogBeanEql(clazz, eqlConnection);
                    // 参数类型注解未指定连接, 且Logger未指定默认连接, 则跳过
                    if (isNull(eql)) continue;

                    val tnps = getTableNamePattern(clazz);
                    val rolling = getTableNameRolling(tnps, context);
                    rolling.rolling(eql, clazz);

                    // 公共参数, 包含event/mdc/ctx-property
                    val paramMap = buildEventMap(eventObject);
                    // 设参数key为arg, 加入eql参数上下文
                    paramMap.put("arg", argument);
                    paramMap.put(ACTIVE_TABLE_NAME, rolling.getActiveTableName());
                    // 同时设置一般参数与动态参数
                    eql.params(paramMap).dynamics(paramMap);

                    // 指定sqlFile的情形
                    if (useEqlLogSql(clazz, eql)) eql.execute();
                        // 根据POJO生成直接SQL
                    else eql.execute(getEqlLogPojoSql(clazz));
                }

            } finally {
                EqlExecuteWrapper.afterExecute(eventObject);
            }
        }
    }
}
