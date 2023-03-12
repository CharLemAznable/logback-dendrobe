package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class KafkaEffectorBuilder implements EffectorBuilder {

    public static final String KAFKA_EFFECTOR = "KAFKA_EFFECTOR";

    @Override
    public String effectorName() {
        return KAFKA_EFFECTOR;
    }
}
