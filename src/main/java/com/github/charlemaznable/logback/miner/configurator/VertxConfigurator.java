package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.VertxAppender;
import com.github.charlemaznable.logback.miner.appender.VertxManager;
import com.google.auto.service.AutoService;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.buildVertxAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.getLogger;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public class VertxConfigurator extends AppenderConfigurator {

    private static final String VERTX_LEVEL_SUFFIX = "[vertx.level]";
    private static final String VERTX_NAME_SUFFIX = "[vertx.name]";
    private static final String VERTX_ADDRESS_SUFFIX = "[vertx.address]";
    private static final String APPENDERS_SUFFIX = "[appenders]";
    private static final String APPENDER_PART = "vertx";

    private ConcurrentMap<String, VertxAppender> appenderMap = new ConcurrentHashMap<>();

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, VERTX_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, VERTX_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name).setVertxLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, VERTX_NAME_SUFFIX)) {
            val name = fetchLoggerName(key, VERTX_NAME_SUFFIX);
            val logger = getLogger(loggerContext, name);
            fetchVertxAppender(logger).setVertxName(value);
            VertxManager.configVertx(value);

        } else if (endsWithIgnoreCase(key, VERTX_ADDRESS_SUFFIX)) {
            val name = fetchLoggerName(key, VERTX_ADDRESS_SUFFIX);
            fetchVertxAppender(getLogger(loggerContext, name)).setVertxAddress(value);

        } else if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            val name = fetchLoggerName(key, APPENDERS_SUFFIX);
            if (containsIgnoreCase(value, APPENDER_PART)) {
                val logger = getLogger(loggerContext, name);
                val appender = fetchVertxAppender(logger);
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

    private VertxAppender fetchVertxAppender(Logger logger) {
        return appenderMap.computeIfAbsent(logger.getName(),
                loggerName -> buildVertxAppender(logger));
    }
}
