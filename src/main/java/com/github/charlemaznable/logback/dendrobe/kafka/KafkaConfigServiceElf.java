package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultKafkaConfigService;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class KafkaConfigServiceElf {

    private static final KafkaConfigService configService;

    static {
        configService = findKafkaConfigService();
    }

    public static KafkaConfigService configService() {
        return configService;
    }

    private static KafkaConfigService findKafkaConfigService() {
        val configServices = ServiceLoader.load(KafkaConfigService.class).iterator();
        if (!configServices.hasNext()) return new DefaultKafkaConfigService();

        val result = configServices.next();
        if (configServices.hasNext())
            throw new IllegalStateException("Multiple KafkaConfigService Defined");
        return result;
    }
}
