package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import com.github.charlemaznable.logback.miner.level.EffectorContext;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.function.BiFunction;

import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;

public abstract class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    protected abstract UnsynchronizedAppenderBase<ILoggingEvent> internalAppend();

    protected abstract FilterReply decide(Effector effector, Level eventLevel);

    @Override
    public void start() {
        this.addAppender(internalAppend());

        internalAppend().start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        internalAppend().stop();

        this.detachAppender(internalAppend());
    }

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
        this.addFilter(new EffectorFilter(effectorContext, this::decide));
    }

    @AllArgsConstructor
    private static class EffectorFilter extends Filter<ILoggingEvent> {

        private EffectorContext effectorContext;
        private BiFunction<Effector, Level, FilterReply> decideFunction;

        @Override
        public FilterReply decide(ILoggingEvent event) {
            val effector = effectorContext.getEffector(event.getLoggerName());
            return decideFunction.apply(effector, event.getLevel());
        }
    }
}
