package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;
import lombok.val;

public abstract class AppenderConfigurator implements Configurator {

    private COWArrayList<Appender> appenderList = new COWArrayList<>(new Appender[0]);

    @Override
    public void before(LoggerContext loggerContext) {
        // empty method
    }

    @Override
    public void finish(LoggerContext loggerContext) {
        for (val appender : appenderList) {
            appender.start();
        }
        appenderList.clear();
    }

    protected void addAppenderIfAbsent(Appender<ILoggingEvent> appender) {
        appenderList.addIfAbsent(appender);
    }
}
