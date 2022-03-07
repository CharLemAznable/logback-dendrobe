package com.github.charlemaznable.logback.dendrobe.appender;

import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.dendrobe.configurator.Configurator;
import com.google.auto.service.AutoService;

@AutoService(Configurator.class)
public class EmptyConfigurator implements Configurator {

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {}
}
