package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.github.charlemaznable.logback.miner.appender.DqlAppender;
import com.github.charlemaznable.logback.miner.appender.VertxAppender;
import lombok.val;

public class ConfiguratorUtil {

    private ConfiguratorUtil() {}

    public static String fetchPropertyKey(String key, String prefix, String suffix) {
        return key.substring(prefix.length(), key.length() - suffix.length());
    }

    public static String fetchLoggerName(String key, String suffix) {
        return key.substring(0, key.length() - suffix.length());
    }

    static Logger getLogger(LoggerContext loggerContext, String name) {
        val logger = loggerContext.getLogger(name);
        logger.setAdditive(false);
        return logger;
    }

    static ConsoleAppender buildConsoleAppender(Logger logger) {
        val consoleAppenderName = "ConsoleAppender-" + logger.getName();
        val consoleAppender = new ConsoleAppender();
        consoleAppender.setName(consoleAppenderName);
        consoleAppender.setContext(logger.getLoggerContext());
        return consoleAppender;
    }

    static DqlAppender buildDqlAppender(Logger logger) {
        val dqlAppenderName = "DqlAppender-" + logger.getName();
        val dqlAppender = new DqlAppender();
        dqlAppender.setName(dqlAppenderName);
        dqlAppender.setContext(logger.getLoggerContext());
        return dqlAppender;
    }

    static VertxAppender buildVertxAppender(Logger logger) {
        val vertxAppenderName = "VertxAppender-" + logger.getName();
        val vertxAppender = new VertxAppender();
        vertxAppender.setName(vertxAppenderName);
        vertxAppender.setContext(logger.getLoggerContext());
        // default vertx address is logger name
        vertxAppender.setVertxAddress(logger.getName());
        return vertxAppender;
    }
}
