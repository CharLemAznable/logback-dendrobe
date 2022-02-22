package com.github.charlemaznable.logback.miner.vertx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.appender.AsyncAppender;
import com.github.charlemaznable.logback.miner.level.Effector;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.github.charlemaznable.logback.miner.appender.LoggingEventElf.buildEventMap;
import static com.github.charlemaznable.logback.miner.vertx.VertxCaches.VertxLogAddressCache.getVertxAddress;
import static com.github.charlemaznable.logback.miner.vertx.VertxCaches.VertxLogBeanPresentCache.isVertxLogBeanPresent;
import static com.github.charlemaznable.logback.miner.vertx.VertxCaches.VertxLogBeanVertxNameCache.getVertxName;
import static com.github.charlemaznable.logback.miner.vertx.VertxEffectorBuilder.VERTX_EFFECTOR;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public final class VertxAppender extends AsyncAppender {

    public static final String DEFAULT_VERTX_NAME = "DEFAULT";

    private InternalAppender appender;

    public VertxAppender() {
        this.appender = new InternalAppender();
        this.appender.setVertxName(DEFAULT_VERTX_NAME);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public VertxAppender setVertxName(String vertxName) {
        this.appender.setVertxName(vertxName);
        return this;
    }

    public VertxAppender setVertxAddress(String vertxAddress) {
        this.appender.setVertxAddress(vertxAddress);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured VertxAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getEffectorLevelInt(VERTX_EFFECTOR) > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Setter
        private String vertxName;
        @Setter
        private String vertxAddress;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            // 公共参数, 包含event/mdc/ctx-property
            val paramMap = buildEventMap(eventObject);

            val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
            val arguments = Arrays.stream(argumentArray)
                    .filter(arg -> nonNull(arg) && isVertxLogBeanPresent(arg.getClass()))
                    .collect(Collectors.toList());
            // 日志不包含@VertxLogBean注解的参数
            if (arguments.isEmpty()) {
                val vertx = VertxManager.getVertx(vertxName);
                if (isNull(vertx) || isNull(vertxAddress)) return;
                vertx.eventBus().publish(vertxAddress, new JsonObject(paramMap));
                return;
            }

            // 遍历@VertxLogBean注解的参数
            for (val argument : arguments) {
                val clazz = argument.getClass();
                val vertx = VertxManager.getVertx(getVertxName(clazz, vertxName));
                val address = getVertxAddress(clazz, vertxAddress);
                if (isNull(vertx) || isNull(address)) continue;

                val currentMap = newHashMap(paramMap);
                currentMap.put("arg", argument);
                vertx.eventBus().publish(address, new JsonObject(currentMap));
            }
        }
    }
}
