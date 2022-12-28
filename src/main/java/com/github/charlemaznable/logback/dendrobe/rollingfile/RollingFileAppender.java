package com.github.charlemaznable.logback.dendrobe.rollingfile;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import com.github.charlemaznable.logback.dendrobe.appender.AsyncOutputStreamAppender;
import com.github.charlemaznable.logback.dendrobe.effect.Effector;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import static ch.qos.logback.core.CoreConstants.UNBOUND_HISTORY;
import static com.github.charlemaznable.logback.dendrobe.rollingfile.RollingFileEffectorBuilder.ROLLING_FILE_EFFECTOR;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class RollingFileAppender extends AsyncOutputStreamAppender {

    public static final String DEFAULT_MAX_FILE_SIZE = "10MB";
    public static final int DEFAULT_MIN_INDEX = 1;
    public static final int DEFAULT_MAX_INDEX = 7;
    public static final int DEFAULT_MAX_HISTORY = UNBOUND_HISTORY;
    public static final boolean DEFAULT_CLEAN_HISTORY_ON_START = false;

    private final ch.qos.logback.core.rolling.RollingFileAppender<ILoggingEvent> appender;
    @Setter
    @Accessors(chain = true)
    private String fileNamePattern;
    @Setter
    @Accessors(chain = true)
    private String maxFileSize = DEFAULT_MAX_FILE_SIZE;
    @Setter
    @Accessors(chain = true)
    private int minIndex = DEFAULT_MIN_INDEX;
    @Setter
    @Accessors(chain = true)
    private int maxIndex = DEFAULT_MAX_INDEX;
    @Setter
    @Accessors(chain = true)
    private int maxHistory = DEFAULT_MAX_HISTORY;
    @Setter
    @Accessors(chain = true)
    private boolean cleanHistoryOnStart = DEFAULT_CLEAN_HISTORY_ON_START;

    public RollingFileAppender() {
        this.appender = new ch.qos.logback.core.rolling.RollingFileAppender<>();
        this.appender.setEncoder(getEncoder());
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public RollingFileAppender setFile(String file) {
        this.appender.setFile(file);
        return this;
    }

    public RollingFileAppender setPrudent(boolean prudent) {
        this.appender.setPrudent(prudent);
        return this;
    }

    public RollingFileAppender setAppend(boolean append) {
        this.appender.setAppend(append);
        return this;
    }

    public RollingFileAppender setBufferSize(String bufferSizeStr) {
        this.appender.setBufferSize(FileSize.valueOf(bufferSizeStr));
        return this;
    }

    public RollingFileAppender setImmediateFlush(boolean immediateFlush) {
        this.appender.setImmediateFlush(immediateFlush);
        return this;
    }

    @Override
    public void start() {
        if (configPolicy()) {
            this.appender.getRollingPolicy().start();
            this.appender.getTriggeringPolicy().start();
            super.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (nonNull(this.appender.getRollingPolicy()))
            this.appender.getRollingPolicy().stop();
        if (nonNull(this.appender.getTriggeringPolicy()))
            this.appender.getTriggeringPolicy().stop();
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured RollingFileAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getEffectorLevelInt(ROLLING_FILE_EFFECTOR) > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    private boolean configPolicy() {
        if (isBlank(fileNamePattern)) return false;

        val pattern = new FileNamePattern(fileNamePattern, this.context);
        val hasDateToken = nonNull(pattern.getPrimaryDateTokenConverter());
        val hasIntegerToken = nonNull(pattern.getIntegerTokenConverter());

        if (hasDateToken && hasIntegerToken) {
            // 同时含有时间和索引模式, 则按时间和文件大小滚动
            val rollingPolicy = new SizeAndTimeBasedRollingPolicy<ILoggingEvent>();
            rollingPolicy.setContext(this.context);
            rollingPolicy.setParent(this.appender);
            rollingPolicy.setFileNamePattern(fileNamePattern);
            rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
            rollingPolicy.setMaxHistory(maxHistory);
            rollingPolicy.setCleanHistoryOnStart(cleanHistoryOnStart);
            this.appender.setRollingPolicy(rollingPolicy);
            return true;

        } else if (hasDateToken) {
            // 仅含有时间模式, 则按时间滚动
            val rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
            rollingPolicy.setContext(this.context);
            rollingPolicy.setParent(this.appender);
            rollingPolicy.setFileNamePattern(fileNamePattern);
            rollingPolicy.setMaxHistory(maxHistory);
            rollingPolicy.setCleanHistoryOnStart(cleanHistoryOnStart);
            this.appender.setRollingPolicy(rollingPolicy);
            return true;

        } else if (hasIntegerToken) {
            // 仅含有索引模式, 则按文件大小滚动
            val rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext(this.context);
            rollingPolicy.setParent(this.appender);
            rollingPolicy.setFileNamePattern(fileNamePattern);
            rollingPolicy.setMinIndex(minIndex);
            rollingPolicy.setMaxIndex(maxIndex);
            this.appender.setRollingPolicy(rollingPolicy);
            val triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(this.context);
            triggeringPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
            this.appender.setTriggeringPolicy(triggeringPolicy);
            return true;

        } else {
            // 不支持的滚动文件名格式
            return false;
        }
    }
}
