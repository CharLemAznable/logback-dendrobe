package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.google.auto.service.AutoService;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.buildConsoleAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.getLogger;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";
    private static final String APPENDERS_SUFFIX = "[appenders]";
    private static final String APPENDER_PART = "console";

    private ConcurrentMap<String, ConsoleAppender> appenderMap = new ConcurrentHashMap<>();

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name).setConsoleLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_CHARSET_SUFFIX);
            fetchConsoleAppender(getLogger(loggerContext, name)).setCharset(value);

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_PATTERN_SUFFIX);
            fetchConsoleAppender(getLogger(loggerContext, name)).setPattern(value);

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_TARGET_SUFFIX);
            fetchConsoleAppender(getLogger(loggerContext, name)).setTarget(value);

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX);
            fetchConsoleAppender(getLogger(loggerContext, name)).setImmediateFlush(toBool(value));

        } else if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            val name = fetchLoggerName(key, APPENDERS_SUFFIX);
            if (containsIgnoreCase(value, APPENDER_PART)) {
                val logger = getLogger(loggerContext, name);
                val appender = fetchConsoleAppender(logger);
                logger.detachAppender(appender.getName());
                logger.addAppender(appender);
                addAppenderIfAbsent(appender);
            }
        }
    }

    @Override
    public void postConfigurate(LoggerContext loggerContext) {
        super.postConfigurate(loggerContext);
        appenderMap.clear();
    }

    private ConsoleAppender fetchConsoleAppender(Logger logger) {
        return appenderMap.computeIfAbsent(logger.getName(),
                loggerName -> buildConsoleAppender(logger));
    }
}
