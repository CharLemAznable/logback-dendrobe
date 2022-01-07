package com.github.charlemaznable.logback.miner;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.google.auto.service.AutoService;

@AutoService(Configurator.class)
public final class LogbackMinerConfigurator extends ContextAwareBase implements Configurator {

    private LogbackMinerDiamondListener diamondListener = new LogbackMinerDiamondListener();

    @Override
    public void configure(LoggerContext loggerContext) {
        addInfo("Setting up logback miner configuration.");
        diamondListener.initLoggerContext(loggerContext);
        diamondListener.configureLoggerContext(loggerContext);
    }
}
