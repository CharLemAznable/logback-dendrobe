package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EqlExecuteWrapper {

    private static ServiceLoader<EqlExecuteExtender> extenders;

    static {
        extenders = ServiceLoader.load(EqlExecuteExtender.class);
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
