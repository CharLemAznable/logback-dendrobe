package com.github.charlemaznable.logback.miner.dql;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface DqlExecuteExtender {

    default void preExecute(ILoggingEvent eventObject) {}

    default void afterExecute(ILoggingEvent eventObject) {}
}
