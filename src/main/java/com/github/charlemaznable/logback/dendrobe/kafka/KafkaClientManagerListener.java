package com.github.charlemaznable.logback.dendrobe.kafka;

public interface KafkaClientManagerListener {

    void configuredKafkaClient(String kafkaName);
}
