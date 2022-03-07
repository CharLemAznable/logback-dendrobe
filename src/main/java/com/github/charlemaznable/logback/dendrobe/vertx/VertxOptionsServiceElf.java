package com.github.charlemaznable.logback.dendrobe.vertx;

import io.vertx.core.VertxOptions;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsString;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static com.github.charlemaznable.core.vertx.VertxElf.parsePropertiesToVertxOptions;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class VertxOptionsServiceElf {

    private static VertxOptionsService optionsService;

    static {
        loadVertxOptionsService();
    }

    public static VertxOptionsService optionsService() {
        return optionsService;
    }

    static void loadVertxOptionsService() {
        optionsService = findVertxOptionsService();
    }

    private static VertxOptionsService findVertxOptionsService() {
        val optionsServices = ServiceLoader.load(VertxOptionsService.class).iterator();
        if (!optionsServices.hasNext()) return new DefaultVertxOptionsService();

        val result = optionsServices.next();
        if (optionsServices.hasNext())
            throw new IllegalStateException("Multiple VertxOptionsService Defined");
        return result;
    }

    static class DefaultVertxOptionsService implements VertxOptionsService {

        @Override
        public String getVertxOptionsValue(String configKey) {
            val filename = String.format("vertx-%s.properties", configKey);
            return classResourceAsString(filename);
        }

        @Override
        public VertxOptions parseVertxOptions(String configKey, String configValue) {
            val properties = parseStringToProperties(configValue);
            return parsePropertiesToVertxOptions(tryDecrypt(properties, configKey));
        }
    }
}
