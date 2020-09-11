package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.val;

import static com.github.charlemaznable.logback.miner.appender.LoggingEventElf.buildEventMap;
import static java.util.Objects.isNull;

public class VertxAppender extends AsyncAppender {

    private InternalAppender appender;

    public VertxAppender() {
        this.appender = new InternalAppender();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public void setVertxName(String vertxName) {
        this.appender.setVertxName(vertxName);
    }

    public void setVertxAddress(String vertxAddress) {
        this.appender.setVertxAddress(vertxAddress);
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured VertxAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getVertxEffectiveLevelInt() > eventLevel.levelInt) {
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

            val vertx = VertxManager.getVertx(vertxName);
            if (isNull(vertx) || isNull(vertxAddress)) return;

            val eventBus = vertx.eventBus();
            val eventMap = buildEventMap(eventObject);
            eventBus.publish(vertxAddress, new JsonObject(eventMap));
        }
    }
}
