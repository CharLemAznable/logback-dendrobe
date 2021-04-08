package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchDqlAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchEffector;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLogger;
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
            addAppenderIfAbsent(fetchDqlAppender(fetchLogger(
                    loggerContext, key, DQL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_LEVEL_SUFFIX)) {
            fetchEffector(loggerContext, key, DQL_LEVEL_SUFFIX)
                    .setDqlLevel(Level.toLevel(value));
            addAppenderIfAbsent(fetchDqlAppender(fetchLogger(
                    loggerContext, key, DQL_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_CONNECTION_SUFFIX)) {
            addAppenderIfAbsent(fetchDqlAppender(fetchLogger(
                    loggerContext, key, DQL_CONNECTION_SUFFIX)).setDqlConnection(value));

        } else if (endsWithIgnoreCase(key, DQL_SQL_SUFFIX)) {
            addAppenderIfAbsent(fetchDqlAppender(fetchLogger(
                    loggerContext, key, DQL_SQL_SUFFIX)).setDqlSql(value));
        }
    }
}
