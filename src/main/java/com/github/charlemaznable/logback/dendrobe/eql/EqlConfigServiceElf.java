package com.github.charlemaznable.logback.dendrobe.eql;

import lombok.NoArgsConstructor;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import java.util.ServiceLoader;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EqlConfigServiceElf {

    private static EqlConfigService configService;

    static {
        loadEqlConfigService();
    }

    public static EqlConfigService configService() {
        return configService;
    }

    static void loadEqlConfigService() {
        configService = findEqlConfigService();
    }

    private static EqlConfigService findEqlConfigService() {
        val configServices = ServiceLoader.load(EqlConfigService.class).iterator();
        if (!configServices.hasNext()) return new DefaultEqlConfigService();

        val result = configServices.next();
        if (configServices.hasNext())
            throw new IllegalStateException("Multiple EqlConfigService Defined");
        return result;
    }

    static class DefaultEqlConfigService implements EqlConfigService {

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
}
