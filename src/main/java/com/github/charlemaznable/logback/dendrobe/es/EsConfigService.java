package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.core.es.EsConfig;

public interface EsConfigService {

    String getEsConfigValue(String configKey);

    EsConfig parseEsConfig(String configKey, String configValue);

    EsBatchConfig parseEsBatchConfig(String configKey, String configValue);
}
