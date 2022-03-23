package com.github.charlemaznable.logback.dendrobe;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.google.auto.service.AutoService;

@AutoService(Configurator.class)
public final class LogbackDendrobeConfigurator extends ContextAwareBase implements Configurator {

    private LogbackDendrobeListener dendrobeListener = new LogbackDendrobeListener();

    @Override
    public void configure(LoggerContext loggerContext) {
        addInfo("Setting up logback dendrobe configuration.");
        dendrobeListener.initLoggerContext(loggerContext);
        dendrobeListener.configureLoggerContext(loggerContext);
    }
}