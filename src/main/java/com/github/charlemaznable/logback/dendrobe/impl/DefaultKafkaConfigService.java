package com.github.charlemaznable.logback.dendrobe.impl;

import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import com.github.charlemaznable.logback.dendrobe.kafka.KafkaConfigService;
import lombok.val;

import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfigElf.parsePropertiesToBatchExecutorConfig;

public class DefaultKafkaConfigService implements KafkaConfigService {

    @Override
    public String getKafkaConfigValue(String configKey) {
        val filename = String.format("kafka-%s.properties", configKey);
        return classResourceAsString(filename);
    }

    @Override
    public Properties parseKafkaConfig(String configKey, String configValue) {
        return tryDecrypt(parseStringToProperties(configValue), configKey);
    }

    @Override
    public BatchExecutorConfig parseBatchExecutorConfig(String configKey, String configValue) {
        val properties = parseStringToProperties(configValue);
        return parsePropertiesToBatchExecutorConfig(tryDecrypt(properties, configKey));
    }
}
