package com.github.charlemaznable.logback.dendrobe.vertx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.dendrobe.appender.AsyncAppender;
import com.github.charlemaznable.logback.dendrobe.effect.Effector;
import com.github.charlemaznable.logback.dendrobe.vertx.VertxCaches.VertxLogAddressCache;
import com.github.charlemaznable.logback.dendrobe.vertx.VertxCaches.VertxLogBeanPresentCache;
import com.github.charlemaznable.logback.dendrobe.vertx.VertxCaches.VertxLogBeanVertxNameCache;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf.buildEventMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public final class VertxAppender extends AsyncAppender {

    public static final String DEFAULT_VERTX_NAME = "DEFAULT";

    private final InternalAppender appender;

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
        if (effector.getEffectorLevelInt(VertxEffectorBuilder.VERTX_EFFECTOR) > eventLevel.levelInt) {
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

            val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
            val arguments = Arrays.stream(argumentArray)
                    .filter(arg -> nonNull(arg) && VertxLogBeanPresentCache.isVertxLogBeanPresent(arg.getClass()))
                    .collect(Collectors.toList());
            // 日志不包含@VertxLogBean注解的参数
            if (arguments.isEmpty()) {
                val vertx = VertxManager.getVertx(vertxName);
                if (isNull(vertx) || isNull(vertxAddress)) return;
                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildEventMap(eventObject);
                vertx.eventBus().publish(vertxAddress, new JsonObject(paramMap));
                return;
            }

            // 遍历@VertxLogBean注解的参数
            for (val argument : arguments) {
                val clazz = argument.getClass();
                val vertx = VertxManager.getVertx(VertxLogBeanVertxNameCache.getVertxName(clazz, vertxName));
                val address = VertxLogAddressCache.getVertxAddress(clazz, vertxAddress);
                if (isNull(vertx) || isNull(address)) continue;

                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildEventMap(eventObject);
                paramMap.put("arg", argument);
                vertx.eventBus().publish(address, new JsonObject(paramMap));
            }
        }
    }
}
