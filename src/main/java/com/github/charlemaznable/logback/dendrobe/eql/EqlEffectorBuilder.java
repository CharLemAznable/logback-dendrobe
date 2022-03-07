package com.github.charlemaznable.logback.dendrobe.eql;

import com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class EqlEffectorBuilder implements EffectorBuilder {

    public static final String EQL_EFFECTOR = "EQL_EFFECTOR";

    @Override
    public String effectorName() {
        return EQL_EFFECTOR;
    }
}
