package com.github.charlemaznable.logback.dendrobe.eql;

import org.n3r.eql.config.EqlConfig;

public interface EqlConfigService {

    String getEqlConfigValue(String configKey);

    EqlConfig parseEqlConfig(String configKey, String configValue);
}
