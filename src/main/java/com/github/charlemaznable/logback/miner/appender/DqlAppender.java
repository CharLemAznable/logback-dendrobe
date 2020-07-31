package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.Setter;
import lombok.val;
import org.n3r.eql.mtcp.MtcpContext;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackBeanDqlCache.getLogbackBeanDql;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackBeanPresentCache.isLogbackBeanPresent;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackPojoSqlCache.getLogbackPojoSql;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackSqlCache.useLogbackSql;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.n3r.eql.mtcp.MtcpContext.TENANT_CODE;
import static org.n3r.eql.mtcp.MtcpContext.TENANT_ID;

public class DqlAppender extends AsyncAppender {

    private InternalAppender appender;

    public DqlAppender() {
        this.appender = new InternalAppender();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public void setDqlConnection(String dqlConnection) {
        this.appender.setDqlConnection(dqlConnection);
    }

    @Override
    public void start() {
        this.addAppender(this.appender);

        this.appender.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.appender.stop();

        this.detachAppender(this.appender);
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        if (effector.getDqlEffectiveLevelInt() > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        private static Map<String, Function<ILoggingEvent, String>> eventConverterMap = newHashMap();

        static {
            eventConverterMap.put("date", new Function<ILoggingEvent, String>() {
                private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

                @Override
                public String apply(ILoggingEvent event) {
                    return sdf.format(new Date(event.getTimeStamp()));
                }
            });
            eventConverterMap.put("level", event -> event.getLevel().toString());
            eventConverterMap.put("thread", ILoggingEvent::getThreadName);
            eventConverterMap.put("logger", ILoggingEvent::getLoggerName);
            eventConverterMap.put("message", ILoggingEvent::getFormattedMessage);
            eventConverterMap.put("class", event -> {
                val cda = event.getCallerData();
                if (cda != null && cda.length > 0) {
                    return cda[0].getClassName();
                } else {
                    return CallerData.NA;
                }
            });
            eventConverterMap.put("method", event -> {
                val cda = event.getCallerData();
                if (cda != null && cda.length > 0) {
                    return cda[0].getMethodName();
                } else {
                    return CallerData.NA;
                }
            });
            eventConverterMap.put("line", event -> {
                val cda = event.getCallerData();
                if (cda != null && cda.length > 0) {
                    return Integer.toString(cda[0].getLineNumber());
                } else {
                    return CallerData.NA;
                }
            });
            eventConverterMap.put("file", event -> {
                val cda = event.getCallerData();
                if (cda != null && cda.length > 0) {
                    return cda[0].getFileName();
                } else {
                    return CallerData.NA;
                }
            });
            eventConverterMap.put("exception", event -> {
                val tp = event.getThrowableProxy();
                if (isNull(tp)) return "";
                val builder = new StringBuilder().append(tp.getClassName()).append(": ")
                        .append(tp.getMessage()).append(CoreConstants.LINE_SEPARATOR);
                for (val step : tp.getStackTraceElementProxyArray()) {
                    builder.append(CoreConstants.TAB).append(step.toString());
                    ThrowableProxyUtil.subjoinPackagingData(builder, step);
                    builder.append(CoreConstants.LINE_SEPARATOR);
                }
                return builder.toString();
            });
        }

        @Setter
        private String dqlConnection;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            // 仅处理含参日志
            val argumentArray = eventObject.getArgumentArray();
            if (isNull(argumentArray)) return;

            // 仅处理@LogbackBean注解参数
            val arguments = Arrays.stream(argumentArray)
                    .filter(arg -> nonNull(arg) && isLogbackBeanPresent(arg.getClass()))
                    .collect(Collectors.toList());
            if (arguments.isEmpty()) return;

            try {
                // this Compatibility maybe extend by ServiceLoader ?

                // Compatible with eql mtcp
                MtcpContext.setTenantId(MDC.get(TENANT_ID));
                MtcpContext.setTenantCode(MDC.get(TENANT_CODE));

                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildParamMap(eventObject);
                for (val argument : arguments) {
                    val clazz = argument.getClass();
                    val dql = getLogbackBeanDql(clazz, dqlConnection);
                    // 参数类型注解未指定连接, 且Logger未指定默认连接, 则跳过
                    if (isNull(dql)) continue;

                    // 设参数key为arg, 加入eql参数上下文
                    val currentMap = newHashMap(paramMap);
                    currentMap.put("arg", argument);
                    // 同时设置一般参数与动态参数
                    dql.params(currentMap).dynamics(currentMap);

                    // 指定sqlFile的情形
                    if (useLogbackSql(clazz, dql)) dql.execute();
                        // 根据POJO生成直接SQL
                    else dql.execute(getLogbackPojoSql(clazz));
                }

            } finally {
                // Compatible with eql mtcp
                MtcpContext.clear();
            }
        }

        private Map<String, Object> buildParamMap(ILoggingEvent eventObject) {
            Map<String, Object> paramMap = newHashMap();

            Map<String, String> eventMap = newHashMap();
            for (val eventEntry : eventConverterMap.entrySet()) {
                eventMap.put(eventEntry.getKey(),
                        eventEntry.getValue().apply(eventObject));
            }
            paramMap.put("event", eventMap);

            Map<String, String> mdcMap = newHashMap();
            for (val mdcEntry : eventObject.getMDCPropertyMap().entrySet()) {
                mdcMap.put(mdcEntry.getKey(), mdcEntry.getValue());
            }
            paramMap.put("mdc", mdcMap);

            Map<String, String> propMap = newHashMap();
            for (val propEntry : eventObject.getLoggerContextVO().getPropertyMap().entrySet()) {
                val key = propEntry.getKey();
                propMap.put(key, defaultIfNull(propEntry.getValue(), System.getProperty(key)));
            }
            paramMap.put("property", propMap);

            return paramMap;
        }
    }
}
