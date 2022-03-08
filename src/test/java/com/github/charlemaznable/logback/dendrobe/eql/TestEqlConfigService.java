package com.github.charlemaznable.logback.dendrobe.eql;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultEqlConfigService;
import com.google.auto.service.AutoService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoService(EqlConfigService.class)
public class TestEqlConfigService extends DefaultEqlConfigService {

    private static Map<String, String> configMap = newHashMap();

    public static void setConfig(String configKey, String configValue) {
        configMap.put(configKey, configValue);
    }

    public static void removeConfig(String configKey) {
        configMap.remove(configKey);
    }

    @Override
    public String getEqlConfigValue(String configKey) {
        assertNull(super.getEqlConfigValue(configKey));
        return configMap.get(configKey);
    }
}
