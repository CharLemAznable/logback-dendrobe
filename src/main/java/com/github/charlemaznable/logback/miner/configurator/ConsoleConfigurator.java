package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchConsoleAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchEffector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLogger;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_ENABLED_SUFFIX = "[console.enabled]";
    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, CONSOLE_ENABLED_SUFFIX)) {
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_ENABLED_SUFFIX)));

        } else if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            fetchEffector(loggerContext, key, CONSOLE_LEVEL_SUFFIX)
                    .setConsoleLevel(Level.toLevel(value));
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_CHARSET_SUFFIX)).setCharset(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_PATTERN_SUFFIX)).setPattern(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_TARGET_SUFFIX)).setTarget(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            addAppenderIfAbsent(fetchConsoleAppender(fetchLogger(
                    loggerContext, key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBool(value)));
        }
    }
}
