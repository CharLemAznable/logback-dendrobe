package com.github.charlemaznable.logback.miner.dql;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class DqlExecuteWrapper {

    private static ServiceLoader<DqlExecuteExtender> extenders;

    static {
        extenders = ServiceLoader.load(DqlExecuteExtender.class);
    }

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
