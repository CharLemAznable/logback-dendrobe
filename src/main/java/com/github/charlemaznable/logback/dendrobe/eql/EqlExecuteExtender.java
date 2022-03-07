package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface EqlExecuteExtender {

    default void preExecute(ILoggingEvent eventObject) {}

    default void afterExecute(ILoggingEvent eventObject) {}
}
