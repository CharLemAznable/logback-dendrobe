package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.dqlAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.effector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.logger;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public class DqlConfigurator extends AppenderConfigurator {

    private static final String DQL_SUFFIX = "[dql]";
    private static final String DQL_LEVEL_SUFFIX = "[dql.level]";
    private static final String DQL_CONNECTION_SUFFIX = "[dql.connection]";
    private static final String DQL_SQL_SUFFIX = "[dql.sql]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, DQL_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(
                    loggerContext, key, DQL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_LEVEL_SUFFIX)) {
            effector(loggerContext, key, DQL_LEVEL_SUFFIX)
                    .setDqlLevel(Level.toLevel(value));
            addAppenderIfAbsent(dqlAppender(logger(
                    loggerContext, key, DQL_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_CONNECTION_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_CONNECTION_SUFFIX)).setDqlConnection(value));

        } else if (endsWithIgnoreCase(key, DQL_SQL_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_SQL_SUFFIX)).setDqlSql(value));
        }
    }
}
