package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.appender.DqlAppender;
import com.google.auto.service.AutoService;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.buildDqlAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.getLogger;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public class DqlConfigurator extends AppenderConfigurator {

    private static final String DQL_LEVEL_SUFFIX = "[dql.level]";
    private static final String DQL_CONNECTION_SUFFIX = "[dql.connection]";
    private static final String DQL_SQL_SUFFIX = "[dql.sql]";
    private static final String APPENDERS_SUFFIX = "[appenders]";
    private static final String APPENDER_PART = "dql";

    private ConcurrentMap<String, DqlAppender> appenderMap = new ConcurrentHashMap<>();

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, DQL_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name).setDqlLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, DQL_CONNECTION_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_CONNECTION_SUFFIX);
            fetchDqlAppender(getLogger(loggerContext, name)).setDqlConnection(value);

        } else if (endsWithIgnoreCase(key, DQL_SQL_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_SQL_SUFFIX);
            fetchDqlAppender(getLogger(loggerContext, name)).setDqlSql(value);

        } else if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            val name = fetchLoggerName(key, APPENDERS_SUFFIX);
            if (containsIgnoreCase(value, APPENDER_PART)) {
                val logger = getLogger(loggerContext, name);
                val appender = fetchDqlAppender(logger);
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

    private DqlAppender fetchDqlAppender(Logger logger) {
        return appenderMap.computeIfAbsent(logger.getName(),
                loggerName -> buildDqlAppender(logger));
    }
}
