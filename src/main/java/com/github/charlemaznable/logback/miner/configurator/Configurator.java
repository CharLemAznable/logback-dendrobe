package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.LoggerContext;

public interface Configurator {

    default void before(LoggerContext loggerContext) {}

    void configurate(LoggerContext loggerContext, String key, String value);

    default void finish(LoggerContext loggerContext) {}
}
