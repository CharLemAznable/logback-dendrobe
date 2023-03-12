package com.github.charlemaznable.logback.dendrobe.kafka;

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
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaEffectorBuilder.KAFKA_EFFECTOR;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.replace;

@AutoService(Configurator.class)
public final class KafkaConfigurator extends AppenderConfigurator {

    private static final String KAFKA_APPENDER_PREFIX = "KafkaAppender-";
    private static final String KAFKA_APPENDER = "[kafka]";
    private static final String KAFKA_LEVEL_SUFFIX = "[kafka.level]";
    private static final String KAFKA_NAME_SUFFIX = "[kafka.name]";
    private static final String KAFKA_TOPIC_SUFFIX = "[kafka.topic]";

    private static final Abbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(128);

    private static KafkaAppender kafkaAppender(Logger logger) {
        val kafkaAppenderName = KAFKA_APPENDER_PREFIX + logger.getName();
        Appender<ILoggingEvent> kafkaAppender = logger.getAppender(kafkaAppenderName);
        if (!(kafkaAppender instanceof KafkaAppender)) {
            logger.detachAppender(kafkaAppender);
            kafkaAppender = new KafkaAppender();
            kafkaAppender.setName(kafkaAppenderName);
            kafkaAppender.setContext(logger.getLoggerContext());
            // default topic is logger name
            ((KafkaAppender) kafkaAppender).setKafkaTopic(defaultTopicName(logger));
            logger.addAppender(kafkaAppender);
        }
        return (KafkaAppender) kafkaAppender;
    }

    private static String defaultTopicName(Logger logger) {
        return replace(abbreviator.abbreviate(logger.getName()), ".", "_");
    }

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, APPENDERS_SUFFIX)) {
            addAppenderIfAbsentAndContains(value, KAFKA_APPENDER,
                    kafkaAppender(logger(loggerContext, key, APPENDERS_SUFFIX)));

        } else if (endsWithIgnoreCase(key, KAFKA_LEVEL_SUFFIX)) {
            effector(loggerContext, key, KAFKA_LEVEL_SUFFIX)
                    .setEffectorLevel(KAFKA_EFFECTOR, Level.toLevel(value));
            addAppenderIfAbsent(kafkaAppender(logger(
                    loggerContext, key, KAFKA_LEVEL_SUFFIX)));

        } else if (endsWithIgnoreCase(key, KAFKA_NAME_SUFFIX)) {
            addAppenderIfAbsent(kafkaAppender(logger(loggerContext,
                    key, KAFKA_NAME_SUFFIX)).setKafkaName(value));
            KafkaClientManager.configKafkaClient(value);

        } else if (endsWithIgnoreCase(key, KAFKA_TOPIC_SUFFIX)) {
            addAppenderIfAbsent(kafkaAppender(logger(loggerContext,
                    key, KAFKA_TOPIC_SUFFIX)).setKafkaTopic(value));
        }
    }
}
