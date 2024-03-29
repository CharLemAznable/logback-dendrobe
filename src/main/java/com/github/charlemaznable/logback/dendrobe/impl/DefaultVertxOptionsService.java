package com.github.charlemaznable.logback.dendrobe.impl;

import com.github.charlemaznable.logback.dendrobe.vertx.VertxOptionsService;
import io.vertx.core.VertxOptions;
import lombok.val;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static com.github.charlemaznable.core.vertx.VertxElf.parsePropertiesToVertxOptions;

public class DefaultVertxOptionsService implements VertxOptionsService {

    @Override
    public String getVertxOptionsValue(String configKey) {
        val filename = String.format("vertx-%s.properties", configKey);
        return classResourceAsString(filename);
    }

    @Override
    public VertxOptions parseVertxOptions(String configKey, String configValue) {
        val properties = parseStringToProperties(configValue);
        return parsePropertiesToVertxOptions(tryDecrypt(properties, configKey));
    }
}
