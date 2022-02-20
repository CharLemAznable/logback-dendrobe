package com.github.charlemaznable.logback.miner.rollingfile;

import com.github.charlemaznable.logback.miner.level.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class RollingFileEffectorBuilder implements EffectorBuilder {

    public static final String ROLLING_FILE_EFFECTOR = "ROLLING_FILE_EFFECTOR";

    @Override
    public String effectorName() {
        return ROLLING_FILE_EFFECTOR;
    }
}
