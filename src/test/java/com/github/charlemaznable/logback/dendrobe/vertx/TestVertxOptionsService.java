package com.github.charlemaznable.logback.dendrobe.vertx;

import com.github.charlemaznable.logback.dendrobe.vertx.VertxOptionsServiceElf.DefaultVertxOptionsService;
import com.google.auto.service.AutoService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoService(VertxOptionsService.class)
public class TestVertxOptionsService extends DefaultVertxOptionsService {

    private static Map<String, String> configMap = newHashMap();

    public static void setConfig(String configKey, String configValue) {
        configMap.put(configKey, configValue);
    }

    public static void removeConfig(String configKey) {
        configMap.remove(configKey);
    }

    @Override
    public String getVertxOptionsValue(String configKey) {
        assertNull(super.getVertxOptionsValue(configKey));
        return configMap.get(configKey);
    }
}
