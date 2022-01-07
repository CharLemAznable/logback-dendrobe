package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import com.github.charlemaznable.logback.miner.level.Effector;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class FileAppender extends AsyncOutputStreamAppender {

    private ch.qos.logback.core.FileAppender<ILoggingEvent> appender;

    public FileAppender() {
        this.appender = new ch.qos.logback.core.FileAppender<>();
        this.appender.setEncoder(getEncoder());
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public FileAppender setFile(String file) {
        this.appender.setFile(file);
        return this;
    }

    public FileAppender setPrudent(boolean prudent) {
        this.appender.setPrudent(prudent);
        return this;
    }

    public FileAppender setAppend(boolean append) {
        this.appender.setAppend(append);
        return this;
    }

    public FileAppender setBufferSize(String bufferSizeStr) {
        this.appender.setBufferSize(FileSize.valueOf(bufferSizeStr));
        return this;
    }

    public FileAppender setImmediateFlush(boolean immediateFlush) {
        this.appender.setImmediateFlush(immediateFlush);
        return this;
    }

    @Override
    public void start() {
        if (isBlank(this.appender.getFile())) return;
        super.start();
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured FileAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getFileEffectiveLevelInt() > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }
}
