package com.github.charlemaznable.logback.dendrobe.kafka;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.dendrobe.appender.AsyncAppender;
import com.github.charlemaznable.logback.dendrobe.effect.Effector;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;

import static com.github.bingoohuang.utils.lang.Mapp.desc;
import static com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf.buildEventMap;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaCaches.KafkaLogBeanKafkaNameCache.getKafkaName;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaCaches.KafkaLogBeanPresentCache.isKafkaLogBeanPresent;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaCaches.KafkaLogTopicCache.getKafkaTopic;
import static com.github.charlemaznable.logback.dendrobe.kafka.KafkaEffectorBuilder.KAFKA_EFFECTOR;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public final class KafkaAppender extends AsyncAppender {

    public static final String DEFAULT_KAFKA_NAME = "DEFAULT";

    private final InternalAppender appender;

    public KafkaAppender() {
        this.appender = new InternalAppender();
        this.appender.setKafkaName(DEFAULT_KAFKA_NAME);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public KafkaAppender setKafkaName(String kafkaName) {
        this.appender.setKafkaName(kafkaName);
        return this;
    }

    public KafkaAppender setKafkaTopic(String kafkaTopic) {
        this.appender.setKafkaTopic(kafkaTopic);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured KafkaAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getEffectorLevelInt(KAFKA_EFFECTOR) > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Setter
        private String kafkaName;
        @Setter
        private String kafkaTopic;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
            val arguments = Arrays.stream(argumentArray)
                    .filter(arg -> nonNull(arg) && isKafkaLogBeanPresent(arg.getClass())).toList();
            // 日志不包含@KafkaLogBean注解的参数
            if (arguments.isEmpty()) {
                val kafkaClient = KafkaClientManager.getKafkaClient(kafkaName);
                if (isNull(kafkaClient) || isNull(kafkaTopic)) return;
                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildEventMap(eventObject);
                kafkaClient.addRecord(kafkaTopic, paramMap.westId(), paramMap);
                return;
            }

            // 遍历@KafkaLogBean注解的参数
            for (val argument : arguments) {
                val clazz = argument.getClass();
                val kafkaClient = KafkaClientManager.getKafkaClient(getKafkaName(clazz, kafkaName));
                val topic = getKafkaTopic(clazz, kafkaTopic);
                if (isNull(kafkaClient) || isNull(topic)) continue;

                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildEventMap(eventObject);
                val currentMap = newHashMap(paramMap);
                currentMap.put("arg", desc(argument)); // trans to map
                kafkaClient.addRecord(topic, paramMap.westId(), currentMap);
            }
        }
    }
}
