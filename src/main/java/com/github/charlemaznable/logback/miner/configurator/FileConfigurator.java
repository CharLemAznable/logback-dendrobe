package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchEffector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchFileAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLogger;
import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class FileConfigurator extends AppenderConfigurator {

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
        if (endsWithIgnoreCase(key, FILE_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_SUFFIX)).setFile(value));

        } else if (endsWithIgnoreCase(key, FILE_LEVEL_SUFFIX)) {
            fetchEffector(loggerContext, key, FILE_LEVEL_SUFFIX)
                    .setFileLevel(Level.toLevel(value));
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, FILE_CHARSET_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_CHARSET_SUFFIX)).setCharset(value));

        } else if (endsWithIgnoreCase(key, FILE_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_PATTERN_SUFFIX)).setPattern(value));

        } else if (endsWithIgnoreCase(key, FILE_PRUDENT_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_PRUDENT_SUFFIX)).setPrudent(toBool(value)));

        } else if (endsWithIgnoreCase(key, FILE_APPEND_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_APPEND_SUFFIX)).setAppend(toBool(value)));

        } else if (endsWithIgnoreCase(key, FILE_BUFFER_SIZE_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_BUFFER_SIZE_SUFFIX)).setBufferSize(parseLong(value)));

        } else if (endsWithIgnoreCase(key, FILE_IMMEDIATE_FLUSH_SUFFIX)) {
            addAppenderIfAbsent(fetchFileAppender(fetchLogger(
                    loggerContext, key, FILE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBool(value)));
        }
    }
}
