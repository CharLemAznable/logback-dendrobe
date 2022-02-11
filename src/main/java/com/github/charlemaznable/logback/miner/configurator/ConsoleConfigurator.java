package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.consoleAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.logger;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public final class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_APPENDER = "[console]";
    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, CONSOLE_APPENDER,
                    consoleAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            effector(loggerContext, key, CONSOLE_LEVEL_SUFFIX)
                    .setConsoleLevel(Level.toLevel(value));
            addAppenderIfAbsent(consoleAppender(logger(
                    loggerContext, key, CONSOLE_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            addAppenderIfAbsent(consoleAppender(logger(loggerContext,
                    key, CONSOLE_CHARSET_SUFFIX)).setCharset(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(consoleAppender(logger(loggerContext,
                    key, CONSOLE_PATTERN_SUFFIX)).setPattern(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            addAppenderIfAbsent(consoleAppender(logger(loggerContext,
                    key, CONSOLE_TARGET_SUFFIX)).setTarget(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            addAppenderIfAbsent(consoleAppender(logger(loggerContext,
                    key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBool(value)));
        }
    }
}
