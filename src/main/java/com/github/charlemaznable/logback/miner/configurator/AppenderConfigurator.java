package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;
import lombok.Getter;

public abstract class AppenderConfigurator implements Configurator {

    @Getter
    private COWArrayList<Appender> appenderList = new COWArrayList<>(new Appender[0]);

    public void clearAppenderList() {
        appenderList.clear();
    }

    protected void addAppenderIfAbsent(Appender<ILoggingEvent> appender) {
        appenderList.addIfAbsent(appender);
    }
}
