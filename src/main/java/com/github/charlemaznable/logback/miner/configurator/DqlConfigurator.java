package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.miner.appender.DqlAppender;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@AutoService(Configurator.class)
public class DqlConfigurator extends AppenderConfigurator {

    private static final String DQL_LEVEL_SUFFIX = "[dql.level]";
    private static final String DQL_CONNECTION_SUFFIX = "[dql.connection]";
    private static final String DQL_SQL_SUFFIX = "[dql.sql]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, DQL_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name).setDqlLevel(Level.toLevel(value));

        } else if (endsWithIgnoreCase(key, DQL_CONNECTION_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_CONNECTION_SUFFIX);
            val dqlAppender = fetchDqlAppender(loggerContext.getLogger(name));
            dqlAppender.setDqlConnection(value);
            addAppenderIfAbsent(dqlAppender);

        } else if (endsWithIgnoreCase(key, DQL_SQL_SUFFIX)) {
            val name = fetchLoggerName(key, DQL_SQL_SUFFIX);
            val dqlAppender = fetchDqlAppender(loggerContext.getLogger(name));
            dqlAppender.setDqlSql(value);
            addAppenderIfAbsent(dqlAppender);

        }
    }

    private DqlAppender fetchDqlAppender(Logger logger) {
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
}
