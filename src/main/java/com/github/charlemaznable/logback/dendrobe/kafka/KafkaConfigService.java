package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;

import java.util.Properties;

public interface KafkaConfigService {

    String getKafkaConfigValue(String configKey);

    Properties parseKafkaConfig(String configKey, String configValue);

    BatchExecutorConfig parseBatchExecutorConfig(String configKey, String configValue);
}
