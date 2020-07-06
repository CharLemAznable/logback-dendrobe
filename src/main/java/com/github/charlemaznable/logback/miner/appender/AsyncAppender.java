package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.val;

import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;

public abstract class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    protected abstract FilterReply decide(Effector effector, Level eventLevel);

    @Override
    protected void preprocess(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        event.getCallerData();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);

        val effectorContext = getEffectorContext(context);
        if (isNull(effectorContext)) return;
        this.addFilter(new AsyncAppenderFilter(effectorContext, this::decide));
    }
}
