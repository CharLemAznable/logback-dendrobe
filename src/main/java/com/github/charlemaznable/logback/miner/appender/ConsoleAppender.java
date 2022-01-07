package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.val;

import java.util.Arrays;

public final class ConsoleAppender extends AsyncOutputStreamAppender {

    private InternalAppender appender;

    public ConsoleAppender() {
        this.appender = new InternalAppender();
        this.appender.setEncoder(getEncoder());
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public ConsoleAppender setTarget(String targetName) {
        this.appender.setTarget(targetName);
        return this;
    }

    public ConsoleAppender setImmediateFlush(boolean immediateFlush) {
        this.appender.setImmediateFlush(immediateFlush);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured ConsoleAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getConsoleEffectiveLevelInt() > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends OutputStreamAppender<ILoggingEvent> {

        private ConsoleTarget target = ConsoleTarget.SYSTEM_OUT;

        @Override
        public void start() {
            setOutputStream(target.getStream());
            super.start();
        }

        public void setTarget(String value) {
            val t = ConsoleTarget.findByName(value.trim());
            if (t == null) {
                targetWarn(value);
            } else {
                target = t;
            }
        }

        private void targetWarn(String val) {
            val status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ConsoleTarget.values()), this);
            status.add(new WarnStatus("Using previously set target, System.out by default.", this));
            addStatus(status);
        }
    }
}
