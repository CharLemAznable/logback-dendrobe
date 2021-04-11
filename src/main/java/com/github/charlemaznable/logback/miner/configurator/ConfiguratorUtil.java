package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.github.charlemaznable.logback.miner.appender.DqlAppender;
import com.github.charlemaznable.logback.miner.appender.FileAppender;
import com.github.charlemaznable.logback.miner.appender.RollingFileAppender;
import com.github.charlemaznable.logback.miner.appender.VertxAppender;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.val;

import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConfiguratorUtil {

    private ConfiguratorUtil() {}

    static String propertyKey(String key, String prefix, String suffix) {
        return key.substring(prefix.length(), key.length() - suffix.length());
    }

    static String loggerName(String key, String suffix) {
        return key.substring(0, key.length() - suffix.length());
    }

    static Logger logger(LoggerContext loggerContext, String name) {
        val logger = loggerContext.getLogger(name);
        logger.setAdditive(false);
        return logger;
    }

    static Logger logger(LoggerContext loggerContext, String key, String suffix) {
        return logger(loggerContext, loggerName(key, suffix));
    }

    static Effector effector(LoggerContext loggerContext, String key, String suffix) {
        val effectorContext = checkNotNull(getEffectorContext(loggerContext));
        return effectorContext.getEffector(loggerName(key, suffix));
    }

    static ConsoleAppender consoleAppender(Logger logger) {
        val consoleAppenderName = "ConsoleAppender-" + logger.getName();
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

    static DqlAppender dqlAppender(Logger logger) {
        val dqlAppenderName = "DqlAppender-" + logger.getName();
        Appender<ILoggingEvent> dqlAppender = logger.getAppender(dqlAppenderName);
        if (!(dqlAppender instanceof DqlAppender)) {
            logger.detachAppender(dqlAppender);
            dqlAppender = new DqlAppender();
            dqlAppender.setName(dqlAppenderName);
            dqlAppender.setContext(logger.getLoggerContext());
            logger.addAppender(dqlAppender);
        }
        return (DqlAppender) dqlAppender;
    }

    static VertxAppender vertxAppender(Logger logger) {
        val vertxAppenderName = "VertxAppender-" + logger.getName();
        Appender<ILoggingEvent> vertxAppender = logger.getAppender(vertxAppenderName);
        if (!(vertxAppender instanceof VertxAppender)) {
            logger.detachAppender(vertxAppender);
            vertxAppender = new VertxAppender();
            vertxAppender.setName(vertxAppenderName);
            vertxAppender.setContext(logger.getLoggerContext());
            // default vertx address is logger name
            ((VertxAppender) vertxAppender).setVertxAddress(logger.getName());
            logger.addAppender(vertxAppender);
        }
        return (VertxAppender) vertxAppender;
    }

    static FileAppender fileAppender(Logger logger) {
        val fileAppenderName = "FileAppender-" + logger.getName();
        Appender<ILoggingEvent> fileAppender = logger.getAppender(fileAppenderName);
        if (!(fileAppender instanceof FileAppender)) {
            logger.detachAppender(fileAppender);
            fileAppender = new FileAppender();
            fileAppender.setName(fileAppenderName);
            fileAppender.setContext(logger.getLoggerContext());
            logger.addAppender(fileAppender);
        }
        return (FileAppender) fileAppender;
    }

    static RollingFileAppender rollingFileAppender(Logger logger) {
        val fileAppenderName = "RollingFileAppender-" + logger.getName();
        Appender<ILoggingEvent> rollingFileAppender = logger.getAppender(fileAppenderName);
        if (!(rollingFileAppender instanceof RollingFileAppender)) {
            logger.detachAppender(rollingFileAppender);
            rollingFileAppender = new RollingFileAppender();
            rollingFileAppender.setName(fileAppenderName);
            rollingFileAppender.setContext(logger.getLoggerContext());
            logger.addAppender(rollingFileAppender);
        }
        return (RollingFileAppender) rollingFileAppender;
    }
}
