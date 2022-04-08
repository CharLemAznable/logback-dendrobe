package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.core.es.EsConfig;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;

public interface EsConfigService {

    String getEsConfigValue(String configKey);

    EsConfig parseEsConfig(String configKey, String configValue);

    BatchExecutorConfig parseBatchExecutorConfig(String configKey, String configValue);
}
