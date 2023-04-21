package com.github.charlemaznable.logback.dendrobe;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.google.auto.service.AutoService;

@AutoService(Configurator.class)
public final class LogbackDendrobeConfigurator extends ContextAwareBase implements Configurator {

    private final LogbackDendrobeListener dendrobeListener = new LogbackDendrobeListener();

    @Override
    public Configurator.ExecutionStatus configure(LoggerContext loggerContext) {
        addInfo("Setting up logback dendrobe configuration.");
        dendrobeListener.initLoggerContext(loggerContext);
        dendrobeListener.configureLoggerContext(loggerContext);
        return dendrobeListener.getBool("logback-invoke-next-configurator", false)
                ? ExecutionStatus.NEUTRAL : ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }
}
