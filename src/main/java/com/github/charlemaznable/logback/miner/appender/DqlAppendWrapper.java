package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.val;

import java.util.ServiceLoader;

class DqlAppendWrapper {

    private static ServiceLoader<DqlAppendExtender> extenders;

    static {
        extenders = ServiceLoader.load(DqlAppendExtender.class);
    }

    private DqlAppendWrapper() {}

    public static void preAppend(ILoggingEvent eventObject) {
        for (val extend : extenders) {
            extend.preAppend(eventObject);
        }
    }

    public static void afterAppend(ILoggingEvent eventObject) {
        for (val extend : extenders) {
            extend.afterAppend(eventObject);
        }
    }
}
