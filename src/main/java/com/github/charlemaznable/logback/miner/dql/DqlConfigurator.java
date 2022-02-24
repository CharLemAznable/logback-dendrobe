package com.github.charlemaznable.logback.miner.dql;

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
import static com.github.charlemaznable.logback.miner.dql.DqlEffectorBuilder.DQL_EFFECTOR;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public final class DqlConfigurator extends AppenderConfigurator {

    private static final String DQL_APPENDER_PREFIX = "DqlAppender-";
    private static final String DQL_APPENDER = "[dql]";
    private static final String DQL_LEVEL_SUFFIX = "[dql.level]";
    private static final String DQL_CONNECTION_SUFFIX = "[dql.connection]";
    private static final String DQL_SQL_SUFFIX = "[dql.sql]";
    private static final String DQL_TABLE_NAME_PATTERN_SUFFIX = "[dql.tableNamePattern]";
    private static final String DQL_PREPARE_SQL_SUFFIX = "[dql.prepareSql]";

    private static DqlAppender dqlAppender(Logger logger) {
        val dqlAppenderName = DQL_APPENDER_PREFIX + logger.getName();
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

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, DQL_APPENDER,
                    dqlAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_LEVEL_SUFFIX)) {
            effector(loggerContext, key, DQL_LEVEL_SUFFIX)
                    .setEffectorLevel(DQL_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(dqlAppender(logger(
                    loggerContext, key, DQL_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, DQL_CONNECTION_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_CONNECTION_SUFFIX)).setDqlConnection(value));

        } else if (endsWithIgnoreCase(key, DQL_SQL_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_SQL_SUFFIX)).setDqlSql(value));

        } else if (endsWithIgnoreCase(key, DQL_TABLE_NAME_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_TABLE_NAME_PATTERN_SUFFIX)).setTableNamePattern(value));

        } else if (endsWithIgnoreCase(key, DQL_PREPARE_SQL_SUFFIX)) {
            addAppenderIfAbsent(dqlAppender(logger(loggerContext,
                    key, DQL_PREPARE_SQL_SUFFIX)).setDqlPrepareSql(value));
        }
    }
}
