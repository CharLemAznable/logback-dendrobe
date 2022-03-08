package com.github.charlemaznable.logback.dendrobe.impl;

import com.github.charlemaznable.logback.dendrobe.eql.EqlConfigService;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;

public class DefaultEqlConfigService implements EqlConfigService {

    @Override
    public String getEqlConfigValue(String configKey) {
        val filename = String.format("eql/eql-%s.properties", configKey);
        return classResourceAsString(filename);
    }

    @Override
    public EqlConfig parseEqlConfig(String configKey, String configValue) {
        val properties = parseStringToProperties(configValue);
        if (properties.isEmpty()) return null;
        return new EqlPropertiesConfig(tryDecrypt(properties, configKey));
    }
}
