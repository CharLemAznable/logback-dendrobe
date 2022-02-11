package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.fileAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.logger;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public final class FileConfigurator extends AppenderConfigurator {

    private static final String FILE_APPENDER = "[file]";
    private static final String FILE_SUFFIX = "[file]";
    private static final String FILE_LEVEL_SUFFIX = "[file.level]";
    private static final String FILE_CHARSET_SUFFIX = "[file.charset]";
    private static final String FILE_PATTERN_SUFFIX = "[file.pattern]";
    private static final String FILE_PRUDENT_SUFFIX = "[file.prudent]";
    private static final String FILE_APPEND_SUFFIX = "[file.append]";
    private static final String FILE_BUFFER_SIZE_SUFFIX = "[file.bufferSize]";
    private static final String FILE_IMMEDIATE_FLUSH_SUFFIX = "[file.immediateFlush]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, FILE_APPENDER,
                    fileAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, FILE_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(
                    loggerContext, key, FILE_SUFFIX)).setFile(value));

        } else if (endsWithIgnoreCase(key, FILE_LEVEL_SUFFIX)) {
            effector(loggerContext, key, FILE_LEVEL_SUFFIX)
                    .setFileLevel(Level.toLevel(value));
            addAppenderIfAbsent(fileAppender(logger(
                    loggerContext, key, FILE_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, FILE_CHARSET_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_CHARSET_SUFFIX)).setCharset(value));

        } else if (endsWithIgnoreCase(key, FILE_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_PATTERN_SUFFIX)).setPattern(value));

        } else if (endsWithIgnoreCase(key, FILE_PRUDENT_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_PRUDENT_SUFFIX)).setPrudent(toBool(value)));

        } else if (endsWithIgnoreCase(key, FILE_APPEND_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_APPEND_SUFFIX)).setAppend(toBool(value)));

        } else if (endsWithIgnoreCase(key, FILE_BUFFER_SIZE_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_BUFFER_SIZE_SUFFIX)).setBufferSize(value));

        } else if (endsWithIgnoreCase(key, FILE_IMMEDIATE_FLUSH_SUFFIX)) {
            addAppenderIfAbsent(fileAppender(logger(loggerContext,
                    key, FILE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBool(value)));
        }
    }
}
