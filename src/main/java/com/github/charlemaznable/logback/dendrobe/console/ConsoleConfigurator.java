package com.github.charlemaznable.logback.dendrobe.console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.dendrobe.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.dendrobe.configurator.Configurator;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.dendrobe.console.ConsoleEffectorBuilder.CONSOLE_EFFECTOR;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@AutoService(Configurator.class)
public final class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_APPENDER_PREFIX = "ConsoleAppender-";
    private static final String CONSOLE_APPENDER = "[console]";
    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    public static ConsoleAppender defaultConsoleAppender(LoggerContext loggerContext) {
        val consoleAppender = new ConsoleAppender();
        consoleAppender.setName(CONSOLE_APPENDER_PREFIX + ROOT_LOGGER_NAME);
        consoleAppender.setContext(loggerContext);
        loggerContext.getLogger(ROOT_LOGGER_NAME).addAppender(consoleAppender);
        return consoleAppender;
    }

    private static ConsoleAppender consoleAppender(Logger logger) {
        val consoleAppenderName = CONSOLE_APPENDER_PREFIX + logger.getName();
        Appender<ILoggingEvent> consoleAppender = logger.getAppender(consoleAppenderName);
        if (!(consoleAppender instanceof ConsoleAppender)) {
            logger.detachAppender(consoleAppender);
            consoleAppender = new ConsoleAppender();
            consoleAppender.setName(consoleAppenderName);
            consoleAppender.setContext(logger.getLoggerContext());
            logger.addAppender(consoleAppender);
        }
        return (ConsoleAppender) consoleAppender;
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, CONSOLE_APPENDER,
                    consoleAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            effector(loggerContext, key, CONSOLE_LEVEL_SUFFIX)
                    .setEffectorLevel(CONSOLE_EFFECTOR, Level.toLevel(value));
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
                    key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)).setImmediateFlush(toBoolean(value)));
        }
    }
}
