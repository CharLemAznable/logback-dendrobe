package com.github.charlemaznable.logback.miner.console;

import com.github.charlemaznable.logback.miner.level.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class ConsoleEffectorBuilder implements EffectorBuilder {

    public static final String CONSOLE_EFFECTOR = "CONSOLE_EFFECTOR";

    @Override
    public String effectorName() {
        return CONSOLE_EFFECTOR;
    }
}
