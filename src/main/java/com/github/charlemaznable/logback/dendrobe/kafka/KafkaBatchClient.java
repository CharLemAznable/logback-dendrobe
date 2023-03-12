package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.core.lang.concurrent.BatchExecutor;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.helpers.Util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.kafka.KafkaClientElf.buildProducer;
import static java.util.Objects.isNull;

public final class KafkaBatchClient extends BatchExecutor<ProducerRecord<String, String>> {

    private final KafkaProducer<String, String> producer;

    public static KafkaBatchClient startClient(Properties kafkaConfig, BatchExecutorConfig batchConfig) {
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return startClient(buildProducer(kafkaConfig), batchConfig);
    }

    public static KafkaBatchClient startClient(KafkaProducer<String, String> producer, BatchExecutorConfig batchConfig) {
        val batchClient = new KafkaBatchClient(producer, batchConfig);
        batchClient.start();
        return batchClient;
    }

    public static KafkaBatchClient stopClient(KafkaBatchClient batchClient) {
        if (isNull(batchClient)) return null;
        batchClient.stop();
        return batchClient;
    }

    public static void closeClient(KafkaBatchClient batchClient) {
        if (isNull(batchClient)) return;
        batchClient.producer.close();
    }

    public KafkaBatchClient(KafkaProducer<String, String> producer, BatchExecutorConfig batchConfig) {
        super(batchConfig);
        this.producer = producer;
    }

    public void addRecord(String topic, String key, Map<String, ?> valueMap) {
        add(new ProducerRecord<>(topic, key, json(valueMap)));
    }

    @Override
    public void batchExecute(List<ProducerRecord<String, String>> items) {
        if (isNull(producer)) return;
        for (val item : items) {
            producer.send(item, (metadata, exception) -> {
                if (isNull(exception)) return;
                Util.report("Kafka send failed", exception);
            });
        }
        producer.flush();
    }
}
