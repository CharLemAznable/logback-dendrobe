package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.WarnStatus;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AsyncOutputStreamAppender extends AsyncAppender {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_PATTERN
            = "%date [%20.20thread] %5level %50.50logger{50}\\(%4.4line\\): %message%n";

    @Getter
    private PatternLayoutEncoder encoder;

    public AsyncOutputStreamAppender() {
        this.encoder = new PatternLayoutEncoder();
        this.encoder.setCharset(DEFAULT_CHARSET);
        this.encoder.setPattern(DEFAULT_PATTERN);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.encoder.setContext(context);
    }

    public AsyncOutputStreamAppender setCharset(String charsetName) {
        try {
            this.encoder.setCharset(Charset.forName(charsetName));
        } catch (Exception e) {
            addStatus(new WarnStatus("Set Charset Error: " + charsetName, this));
            this.encoder.setCharset(DEFAULT_CHARSET);
        }
        return this;
    }

    public AsyncOutputStreamAppender setPattern(String patternString) {
        this.encoder.setPattern(patternString);
        return this;
    }

    @Override
    public void start() {
        this.encoder.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.encoder.stop();
    }
}
