package com.github.charlemaznable.logback.dendrobe.eql;

import lombok.val;
import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;

import static com.github.charlemaznable.logback.dendrobe.eql.EqlConfigServiceElf.configService;

public class TestEql extends Eql {

    public TestEql(String configKey) {
        super(createEqlConfig(configKey), Eql.STACKTRACE_DEEP_FIVE);
    }

    public static EqlConfig createEqlConfig(String configKey) {
        val configValue = configService().getEqlConfigValue(configKey);
        return configService().parseEqlConfig(configKey, configValue);
    }
}
