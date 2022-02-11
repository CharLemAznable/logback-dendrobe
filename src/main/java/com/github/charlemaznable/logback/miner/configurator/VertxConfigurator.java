package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.VertxManager;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorElf.vertxAppender;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public final class VertxConfigurator extends AppenderConfigurator {

    private static final String VERTX_APPENDER = "[vertx]";
    private static final String VERTX_LEVEL_SUFFIX = "[vertx.level]";
    private static final String VERTX_NAME_SUFFIX = "[vertx.name]";
    private static final String VERTX_ADDRESS_SUFFIX = "[vertx.address]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, VERTX_APPENDER,
                    vertxAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, VERTX_LEVEL_SUFFIX)) {
            effector(loggerContext, key, VERTX_LEVEL_SUFFIX)
                    .setVertxLevel(Level.toLevel(value));
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
