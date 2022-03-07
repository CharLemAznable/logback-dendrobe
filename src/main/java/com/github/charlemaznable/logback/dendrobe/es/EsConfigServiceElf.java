package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.core.es.EsConfig;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static com.github.charlemaznable.core.es.EsClientElf.parsePropertiesToEsConfig;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EsConfigServiceElf {

    private static EsConfigService configService;

    static {
        loadEsConfigService();
    }

    public static EsConfigService configService() {
        return configService;
    }

    static void loadEsConfigService() {
        configService = findEsConfigService();
    }

    private static EsConfigService findEsConfigService() {
        val configServices = ServiceLoader.load(EsConfigService.class).iterator();
        if (!configServices.hasNext()) return new DefaultEsConfigService();

        val result = configServices.next();
        if (configServices.hasNext())
            throw new IllegalStateException("Multiple EsConfigService Defined");
        return result;
    }

    static class DefaultEsConfigService implements EsConfigService {

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
}
