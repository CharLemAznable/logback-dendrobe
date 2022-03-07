package com.github.charlemaznable.logback.dendrobe.vertx;

import com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class VertxEffectorBuilder implements EffectorBuilder {

    public static final String VERTX_EFFECTOR = "VERTX_EFFECTOR";

    @Override
    public String effectorName() {
        return VERTX_EFFECTOR;
    }
}
