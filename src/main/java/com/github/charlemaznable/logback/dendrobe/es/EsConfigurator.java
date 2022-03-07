package com.github.charlemaznable.logback.dendrobe.es;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.Abbreviator;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.charlemaznable.logback.dendrobe.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.dendrobe.configurator.Configurator;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.effector;
import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.dendrobe.es.EsEffectorBuilder.ES_EFFECTOR;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.replace;

@AutoService(Configurator.class)
public final class EsConfigurator extends AppenderConfigurator {

    private static final String ES_APPENDER_PREFIX = "EsAppender-";
    private static final String ES_APPENDER = "[es]";
    private static final String ES_LEVEL_SUFFIX = "[es.level]";
    private static final String ES_NAME_SUFFIX = "[es.name]";
    private static final String ES_INDEX_SUFFIX = "[es.index]";

    private static final Abbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(128);

    private static EsAppender esAppender(Logger logger) {
        val esAppenderName = ES_APPENDER_PREFIX + logger.getName();
        Appender<ILoggingEvent> esAppender = logger.getAppender(esAppenderName);
        if (!(esAppender instanceof EsAppender)) {
            logger.detachAppender(esAppender);
            esAppender = new EsAppender();
            esAppender.setName(esAppenderName);
            esAppender.setContext(logger.getLoggerContext());
            // default index is logger name
            ((EsAppender) esAppender).setEsIndex(defaultIndexName(logger));
            logger.addAppender(esAppender);
        }
        return (EsAppender) esAppender;
    }

    private static String defaultIndexName(Logger logger) {
        return replace(abbreviator.abbreviate(logger.getName()), ".", "_");
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, ES_APPENDER,
                    esAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, ES_LEVEL_SUFFIX)) {
            effector(loggerContext, key, ES_LEVEL_SUFFIX)
                    .setEffectorLevel(ES_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(esAppender(logger(
                    loggerContext, key, ES_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, ES_NAME_SUFFIX)) {
            addAppenderIfAbsent(esAppender(logger(loggerContext,
                    key, ES_NAME_SUFFIX)).setEsName(value));
            EsClientManager.configEsClient(value);

        } else if (endsWithIgnoreCase(key, ES_INDEX_SUFFIX)) {
            addAppenderIfAbsent(esAppender(logger(loggerContext,
                    key, ES_INDEX_SUFFIX)).setEsIndex(value));
        }
    }
}
