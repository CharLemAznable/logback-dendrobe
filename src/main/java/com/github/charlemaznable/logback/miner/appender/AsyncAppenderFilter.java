package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import com.github.charlemaznable.logback.miner.level.EffectorContext;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.function.BiFunction;

@AllArgsConstructor
public class AsyncAppenderFilter extends Filter<ILoggingEvent> {

    private EffectorContext effectorContext;
    private BiFunction<Effector, Level, FilterReply> decideFunction;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        val effector = effectorContext.getEffector(event.getLoggerName());
        return decideFunction.apply(effector, event.getLevel());
    }
}
