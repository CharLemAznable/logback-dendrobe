package com.github.charlemaznable.logback.dendrobe.configurator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;
import lombok.Getter;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

public abstract class AppenderConfigurator implements Configurator {

    protected static final String APPENDERS_SUFFIX = "[appenders]";

    @SuppressWarnings("rawtypes")
    @Getter
    private final COWArrayList<Appender> appenderList = new COWArrayList<>(new Appender[0]);

    public void clearAppenderList() {
        appenderList.clear();
    }

    protected void addAppenderIfAbsent(Appender<ILoggingEvent> appender) {
        appenderList.addIfAbsent(appender);
    }

    protected void addAppenderIfAbsentAndContains(String value, String name,
                                                  Appender<ILoggingEvent> appender) {
        if (containsIgnoreCase(value, name)) addAppenderIfAbsent(appender);
    }
}
