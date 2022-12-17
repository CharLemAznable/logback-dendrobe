package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultEsConfigService;
import com.google.auto.service.AutoService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoService(EsConfigService.class)
public class TestEsConfigService extends DefaultEsConfigService {

    private static final Map<String, String> configMap = newHashMap();

    public static void setConfig(String configKey, String configValue) {
        configMap.put(configKey, configValue);
    }

    public static void removeConfig(String configKey) {
        configMap.remove(configKey);
    }

    @Override
    public String getEsConfigValue(String configKey) {
        assertNull(super.getEsConfigValue(configKey));
        return configMap.get(configKey);
    }
}
