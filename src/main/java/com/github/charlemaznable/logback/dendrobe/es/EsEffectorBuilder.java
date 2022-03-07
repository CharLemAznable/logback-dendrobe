package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class EsEffectorBuilder implements EffectorBuilder {

    public static final String ES_EFFECTOR = "ES_EFFECTOR";

    @Override
    public String effectorName() {
        return ES_EFFECTOR;
    }
}
