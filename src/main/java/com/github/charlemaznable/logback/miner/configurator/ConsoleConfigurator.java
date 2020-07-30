package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@AutoService(Configurator.class)
public class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    @Override
    public void before(LoggerContext loggerContext) {
        addAppenderIfAbsent(fetchConsoleAppender(loggerContext.getLogger(ROOT_LOGGER_NAME)));
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            var name = fetchLoggerName(key, CONSOLE_LEVEL_SUFFIX);
            var effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name)
                    .setConsoleLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            var name = fetchLoggerName(key, CONSOLE_CHARSET_SUFFIX);
            var consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setCharset(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            var name = fetchLoggerName(key, CONSOLE_PATTERN_SUFFIX);
            var consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setPattern(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            var name = fetchLoggerName(key, CONSOLE_TARGET_SUFFIX);
            var consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setTarget(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            var name = fetchLoggerName(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX);
            var consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setImmediateFlush(toBool(value));
            addAppenderIfAbsent(consoleAppender);

        }
    }

    private ConsoleAppender fetchConsoleAppender(Logger logger) {
        var consoleAppenderName = "ConsoleAppender-" + logger.getName();
        var consoleAppender = logger.getAppender(consoleAppenderName);
        if (!(consoleAppender instanceof ConsoleAppender)) {
            logger.detachAppender(consoleAppender);
            consoleAppender = new ConsoleAppender();
            consoleAppender.setName(consoleAppenderName);
            consoleAppender.setContext(logger.getLoggerContext());
            logger.addAppender(consoleAppender);
        }
        return (ConsoleAppender) consoleAppender;
    }
}
