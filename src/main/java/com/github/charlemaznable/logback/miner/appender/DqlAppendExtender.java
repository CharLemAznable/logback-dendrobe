package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface DqlAppendExtender {

    default void preAppend(ILoggingEvent eventObject) {}

    default void afterAppend(ILoggingEvent eventObject) {}
}
