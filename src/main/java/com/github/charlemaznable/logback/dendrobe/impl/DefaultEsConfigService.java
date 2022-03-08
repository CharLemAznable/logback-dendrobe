package com.github.charlemaznable.logback.dendrobe.impl;

import com.github.charlemaznable.core.es.EsConfig;
import com.github.charlemaznable.logback.dendrobe.es.EsConfigService;
import lombok.val;

import static com.github.charlemaznable.core.es.EsClientElf.parsePropertiesToEsConfig;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;

public class DefaultEsConfigService implements EsConfigService {

    @Override
    public String getEsConfigValue(String configKey) {
        val filename = String.format("es-%s.properties", configKey);
        return classResourceAsString(filename);
    }

    @Override
    public EsConfig parseEsConfig(String configKey, String configValue) {
        val properties = parseStringToProperties(configValue);
        return parsePropertiesToEsConfig(tryDecrypt(properties, configKey));
    }
}
