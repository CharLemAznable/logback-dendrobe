package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultEsConfigService;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

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
}
