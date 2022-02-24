package com.github.charlemaznable.logback.miner.vertx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.miner.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.miner.configurator.Configurator;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.miner.vertx.VertxEffectorBuilder.VERTX_EFFECTOR;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public final class VertxConfigurator extends AppenderConfigurator {

    private static final String VERTX_APPENDER_PREFIX = "VertxAppender-";
    private static final String VERTX_APPENDER = "[vertx]";
    private static final String VERTX_LEVEL_SUFFIX = "[vertx.level]";
    private static final String VERTX_NAME_SUFFIX = "[vertx.name]";
    private static final String VERTX_ADDRESS_SUFFIX = "[vertx.address]";

    private static VertxAppender vertxAppender(Logger logger) {
        val vertxAppenderName = VERTX_APPENDER_PREFIX + logger.getName();
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

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, VERTX_APPENDER,
                    vertxAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, VERTX_LEVEL_SUFFIX)) {
            effector(loggerContext, key, VERTX_LEVEL_SUFFIX)
                    .setEffectorLevel(VERTX_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(vertxAppender(logger(
                    loggerContext, key, VERTX_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, VERTX_NAME_SUFFIX)) {
            addAppenderIfAbsent(vertxAppender(logger(loggerContext,
                    key, VERTX_NAME_SUFFIX)).setVertxName(value));
            VertxManager.configVertx(value);

        } else if (endsWithIgnoreCase(key, VERTX_ADDRESS_SUFFIX)) {
            addAppenderIfAbsent(vertxAppender(logger(loggerContext,
                    key, VERTX_ADDRESS_SUFFIX)).setVertxAddress(value));
        }
    }
}
