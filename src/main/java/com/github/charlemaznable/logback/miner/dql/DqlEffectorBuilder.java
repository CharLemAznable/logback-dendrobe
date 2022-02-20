package com.github.charlemaznable.logback.miner.dql;

import com.github.charlemaznable.logback.miner.level.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class DqlEffectorBuilder implements EffectorBuilder {

    public static final String DQL_EFFECTOR = "DQL_EFFECTOR";

    @Override
    public String effectorName() {
        return DQL_EFFECTOR;
    }
}
