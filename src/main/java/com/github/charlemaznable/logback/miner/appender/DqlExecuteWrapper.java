package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.val;

import java.util.ServiceLoader;

class DqlExecuteWrapper {

    private static ServiceLoader<DqlExecuteExtender> extenders;

    static {
        extenders = ServiceLoader.load(DqlExecuteExtender.class);
    }

    private DqlExecuteWrapper() {}

    public static void preExecute(ILoggingEvent eventObject) {
        for (val extend : extenders) {
            extend.preExecute(eventObject);
        }
    }

    public static void afterExecute(ILoggingEvent eventObject) {
        for (val extend : extenders) {
            extend.afterExecute(eventObject);
        }
    }
}
