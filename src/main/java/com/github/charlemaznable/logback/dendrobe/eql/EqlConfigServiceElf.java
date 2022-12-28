package com.github.charlemaznable.logback.dendrobe.eql;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultEqlConfigService;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EqlConfigServiceElf {

    private static final EqlConfigService configService;

    static {
        configService = findEqlConfigService();
    }

    public static EqlConfigService configService() {
        return configService;
    }

    private static EqlConfigService findEqlConfigService() {
        val configServices = ServiceLoader.load(EqlConfigService.class).iterator();
        if (!configServices.hasNext()) return new DefaultEqlConfigService();

        val result = configServices.next();
        if (configServices.hasNext())
            throw new IllegalStateException("Multiple EqlConfigService Defined");
        return result;
    }
}
