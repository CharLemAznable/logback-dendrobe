package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.FileSize;
import com.github.charlemaznable.logback.miner.level.Effector;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.isNull;

public class FileAppender extends AsyncAppender {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_CONSOLE_PATTERN
            = "%date [%20.20thread] %5level %50.50logger{50}\\(%4.4line\\): %message%n";
    public static final boolean DEFAULT_PRUDENT = false;
    public static final boolean DEFAULT_APPEND = true;
    public static final long DEFAULT_BUFFER_SIZE = 8192;
    public static final boolean DEFAULT_IMMEDIATE_FLUSH = true;

    private PatternLayoutEncoder encoder;
    private ch.qos.logback.core.FileAppender<ILoggingEvent> appender;

    public FileAppender() {
        this.encoder = new PatternLayoutEncoder();
        this.encoder.setCharset(DEFAULT_CHARSET);
        this.encoder.setPattern(DEFAULT_CONSOLE_PATTERN);

        this.appender = new ch.qos.logback.core.FileAppender<>();
        this.appender.setPrudent(DEFAULT_PRUDENT);
        this.appender.setAppend(DEFAULT_APPEND);
        this.appender.setBufferSize(new FileSize(DEFAULT_BUFFER_SIZE));
        this.appender.setImmediateFlush(DEFAULT_IMMEDIATE_FLUSH);
        this.appender.setEncoder(this.encoder);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
        this.encoder.setContext(context);
    }

    public FileAppender setCharset(String charsetName) {
        try {
            this.encoder.setCharset(Charset.forName(charsetName));
        } catch (Exception e) {
            addStatus(new WarnStatus("Set Charset Error: " + charsetName, this));
            this.encoder.setCharset(DEFAULT_CHARSET);
        }
        return this;
    }

    public FileAppender setPattern(String patternString) {
        this.encoder.setPattern(patternString);
        return this;
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

    public FileAppender setBufferSize(long bufferSize) {
        this.appender.setBufferSize(new FileSize(bufferSize));
        return this;
    }

    public FileAppender setImmediateFlush(boolean immediateFlush) {
        this.appender.setImmediateFlush(immediateFlush);
        return this;
    }

    @Override
    public void start() {
        if (isNull(this.appender.getFile())) return;

        this.encoder.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.encoder.stop();
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
