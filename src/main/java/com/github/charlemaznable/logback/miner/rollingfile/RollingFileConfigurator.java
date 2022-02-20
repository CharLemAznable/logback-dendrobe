package com.github.charlemaznable.logback.miner.rollingfile;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.miner.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.miner.configurator.Configurator;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.miner.rollingfile.RollingFileEffectorBuilder.ROLLING_FILE_EFFECTOR;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public final class RollingFileConfigurator extends AppenderConfigurator {

    private static final String ROLLING_FILE_APPENDER_PREFIX = "RollingFileAppender-";
    private static final String ROLLING_FILE_APPENDER = "[rollingfile]";
    private static final String ROLLING_FILE_SUFFIX = "[rollingfile]";
    private static final String ROLLING_FILE_LEVEL_SUFFIX = "[rollingfile.level]";
    private static final String ROLLING_FILE_CHARSET_SUFFIX = "[rollingfile.charset]";
    private static final String ROLLING_FILE_PATTERN_SUFFIX = "[rollingfile.pattern]";
    private static final String ROLLING_FILE_PRUDENT_SUFFIX = "[rollingfile.prudent]";
    private static final String ROLLING_FILE_APPEND_SUFFIX = "[rollingfile.append]";
    private static final String ROLLING_FILE_BUFFER_SIZE_SUFFIX = "[rollingfile.bufferSize]";
    private static final String ROLLING_FILE_IMMEDIATE_FLUSH_SUFFIX = "[rollingfile.immediateFlush]";
    private static final String ROLLING_FILE_NAME_PATTERN_SUFFIX = "[rollingfile.fileNamePattern]";
    private static final String ROLLING_FILE_MAX_FILE_SIZE_SUFFIX = "[rollingfile.maxFileSize]";
    private static final String ROLLING_FILE_MIN_INDEX_SUFFIX = "[rollingfile.minIndex]";
    private static final String ROLLING_FILE_MAX_INDEX_SUFFIX = "[rollingfile.maxIndex]";
    private static final String ROLLING_FILE_MAX_HISTORY_SUFFIX = "[rollingfile.maxHistory]";
    private static final String ROLLING_FILE_CLEAN_HISTORY_ON_START_SUFFIX = "[rollingfile.cleanHistoryOnStart]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, ROLLING_FILE_APPENDER,
                    rollingFileAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_SUFFIX)).setFile(defaultIfBlank(value, null)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_LEVEL_SUFFIX)) {
            effector(loggerContext, key, ROLLING_FILE_LEVEL_SUFFIX)
                    .setEffectorLevel(ROLLING_FILE_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(rollingFileAppender(logger(
                    loggerContext, key, ROLLING_FILE_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_CHARSET_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_CHARSET_SUFFIX)).setCharset(value));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_PATTERN_SUFFIX)).setPattern(value));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_PRUDENT_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_PRUDENT_SUFFIX)).setPrudent(toBool(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_APPEND_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_APPEND_SUFFIX)).setAppend(toBool(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_BUFFER_SIZE_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_BUFFER_SIZE_SUFFIX)).setBufferSize(value));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_IMMEDIATE_FLUSH_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBool(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_NAME_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_NAME_PATTERN_SUFFIX)).setFileNamePattern(value));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_MAX_FILE_SIZE_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_MAX_FILE_SIZE_SUFFIX)).setMaxFileSize(value));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_MIN_INDEX_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_MIN_INDEX_SUFFIX)).setMinIndex(parseInt(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_MAX_INDEX_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_MAX_INDEX_SUFFIX)).setMaxIndex(parseInt(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_MAX_HISTORY_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_MAX_HISTORY_SUFFIX)).setMaxHistory(parseInt(value)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_CLEAN_HISTORY_ON_START_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_CLEAN_HISTORY_ON_START_SUFFIX)).setCleanHistoryOnStart(toBool(value)));
        }
    }

    static RollingFileAppender rollingFileAppender(Logger logger) {
        val fileAppenderName = ROLLING_FILE_APPENDER_PREFIX + logger.getName();
        Appender<ILoggingEvent> rollingFileAppender = logger.getAppender(fileAppenderName);
        if (!(rollingFileAppender instanceof RollingFileAppender)) {
            logger.detachAppender(rollingFileAppender);
            rollingFileAppender = new RollingFileAppender();
            rollingFileAppender.setName(fileAppenderName);
            rollingFileAppender.setContext(logger.getLoggerContext());
            logger.addAppender(rollingFileAppender);
        }
        return (RollingFileAppender) rollingFileAppender;
    }
}
