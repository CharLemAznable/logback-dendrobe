package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import com.github.charlemaznable.logback.miner.level.EffectorContext;

public abstract class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    public AsyncAppender(EffectorContext effectorContext) {
        this.addFilter(new AsyncAppenderFilter(effectorContext, this::decide));
    }

    protected abstract FilterReply decide(Effector effector, Level eventLevel);

    @Override
    protected void preprocess(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        event.getCallerData();
    }
}
