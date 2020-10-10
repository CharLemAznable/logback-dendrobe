package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.LoggerContext;

public interface Configurator {

    void configurate(LoggerContext loggerContext, String key, String value);

    default void postConfigurate(LoggerContext loggerContext) {}
}
