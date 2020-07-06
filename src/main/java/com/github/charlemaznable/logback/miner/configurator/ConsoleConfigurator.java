package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.google.auto.service.AutoService;
import lombok.val;
import lombok.var;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@AutoService(Configurator.class)
public class ConsoleConfigurator implements Configurator {

    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    private COWArrayList<Appender> appenderList = new COWArrayList<>(new Appender[0]);

    @Override
    public void before(LoggerContext loggerContext) {
        appenderList.add(fetchConsoleAppender(loggerContext.getLogger(ROOT_LOGGER_NAME)));
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name)
                    .setConsoleLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_CHARSET_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setCharset(value);
            appenderList.addIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_PATTERN_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setPattern(value);
            appenderList.addIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_TARGET_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setTarget(value);
            appenderList.addIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setImmediateFlush(toBool(value));
            appenderList.addIfAbsent(consoleAppender);

        }
    }

    @Override
    public void finish(LoggerContext loggerContext) {
        for (val appender : appenderList) {
            appender.start();
        }
        appenderList.clear();
    }

    private ConsoleAppender fetchConsoleAppender(Logger logger) {
        val consoleAppenderName = "ConsoleAppender-" + logger.getName();
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