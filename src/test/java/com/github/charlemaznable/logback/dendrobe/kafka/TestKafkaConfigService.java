package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultKafkaConfigService;
import com.google.auto.service.AutoService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoService(KafkaConfigService.class)
public class TestKafkaConfigService extends DefaultKafkaConfigService {

    private static final Map<String, String> configMap = newHashMap();

    public static void setConfig(String configKey, String configValue) {
        configMap.put(configKey, configValue);
    }

    public static void removeConfig(String configKey) {
        configMap.remove(configKey);
    }

    @Override
    public String getKafkaConfigValue(String configKey) {
        assertNull(super.getKafkaConfigValue(configKey));
        return configMap.get(configKey);
    }
}
