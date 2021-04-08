package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.VertxManager;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchEffector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLogger;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchVertxAppender;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public class VertxConfigurator extends AppenderConfigurator {

    private static final String VERTX_ENABLED_SUFFIX = "[vertx.enabled]";
    private static final String VERTX_LEVEL_SUFFIX = "[vertx.level]";
    private static final String VERTX_NAME_SUFFIX = "[vertx.name]";
    private static final String VERTX_ADDRESS_SUFFIX = "[vertx.address]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, VERTX_ENABLED_SUFFIX)) {
            addAppenderIfAbsent(fetchVertxAppender(fetchLogger(
                    loggerContext, key, VERTX_ENABLED_SUFFIX)));

        } else if (endsWithIgnoreCase(key, VERTX_LEVEL_SUFFIX)) {
            fetchEffector(loggerContext, key, VERTX_LEVEL_SUFFIX)
                    .setVertxLevel(Level.toLevel(value));
            addAppenderIfAbsent(fetchVertxAppender(fetchLogger(
                    loggerContext, key, VERTX_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, VERTX_NAME_SUFFIX)) {
            addAppenderIfAbsent(fetchVertxAppender(fetchLogger(
                    loggerContext, key, VERTX_NAME_SUFFIX)).setVertxName(value));
            VertxManager.configVertx(value);

        } else if (endsWithIgnoreCase(key, VERTX_ADDRESS_SUFFIX)) {
            addAppenderIfAbsent(fetchVertxAppender(fetchLogger(
                    loggerContext, key, VERTX_ADDRESS_SUFFIX)).setVertxAddress(value));
        }
    }
}
