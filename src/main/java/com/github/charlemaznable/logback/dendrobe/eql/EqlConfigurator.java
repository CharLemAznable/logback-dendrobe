package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.dendrobe.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.dendrobe.configurator.Configurator;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlEffectorBuilder.EQL_EFFECTOR;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public final class EqlConfigurator extends AppenderConfigurator {

    private static final String EQL_APPENDER_PREFIX = "EqlAppender-";
    private static final String EQL_APPENDER = "[eql]";
    private static final String EQL_LEVEL_SUFFIX = "[eql.level]";
    private static final String EQL_CONNECTION_SUFFIX = "[eql.connection]";
    private static final String EQL_SQL_SUFFIX = "[eql.sql]";
    private static final String EQL_TABLE_NAME_PATTERN_SUFFIX = "[eql.tableNamePattern]";
    private static final String EQL_PREPARE_SQL_SUFFIX = "[eql.prepareSql]";

    private static EqlAppender eqlAppender(Logger logger) {
        val eqlAppenderName = EQL_APPENDER_PREFIX + logger.getName();
        Appender<ILoggingEvent> eqlAppender = logger.getAppender(eqlAppenderName);
        if (!(eqlAppender instanceof EqlAppender)) {
            logger.detachAppender(eqlAppender);
            eqlAppender = new EqlAppender();
            eqlAppender.setName(eqlAppenderName);
            eqlAppender.setContext(logger.getLoggerContext());
            logger.addAppender(eqlAppender);
        }
        return (EqlAppender) eqlAppender;
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, EQL_APPENDER,
                    eqlAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, EQL_LEVEL_SUFFIX)) {
            effector(loggerContext, key, EQL_LEVEL_SUFFIX)
                    .setEffectorLevel(EQL_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(eqlAppender(logger(
                    loggerContext, key, EQL_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, EQL_CONNECTION_SUFFIX)) {
            addAppenderIfAbsent(eqlAppender(logger(loggerContext,
                    key, EQL_CONNECTION_SUFFIX)).setEqlConnection(value));

        } else if (endsWithIgnoreCase(key, EQL_SQL_SUFFIX)) {
            addAppenderIfAbsent(eqlAppender(logger(loggerContext,
                    key, EQL_SQL_SUFFIX)).setEqlSql(value));

        } else if (endsWithIgnoreCase(key, EQL_TABLE_NAME_PATTERN_SUFFIX)) {
            addAppenderIfAbsent(eqlAppender(logger(loggerContext,
                    key, EQL_TABLE_NAME_PATTERN_SUFFIX)).setTableNamePattern(value));

        } else if (endsWithIgnoreCase(key, EQL_PREPARE_SQL_SUFFIX)) {
            addAppenderIfAbsent(eqlAppender(logger(loggerContext,
                    key, EQL_PREPARE_SQL_SUFFIX)).setEqlPrepareSql(value));
        }
    }
}
