package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.logger;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.rollingFileAppender;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class RollingFileConfigurator extends AppenderConfigurator {

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
        if (endsWithIgnoreCase(key, ROLLING_FILE_SUFFIX)) {
            addAppenderIfAbsent(rollingFileAppender(logger(loggerContext,
                    key, ROLLING_FILE_SUFFIX)).setFile(defaultIfBlank(value, null)));

        } else if (endsWithIgnoreCase(key, ROLLING_FILE_LEVEL_SUFFIX)) {
            effector(loggerContext, key, ROLLING_FILE_LEVEL_SUFFIX)
                    .setRollingFileLevel(Level.toLevel(value));
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
}
