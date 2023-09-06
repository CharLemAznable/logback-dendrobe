package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EqlExecuteWrapper {

    private static final CopyOnWriteArrayList<EqlExecuteExtender> extenders;

    static {
        extenders = StreamSupport
                .stream(ServiceLoader.load(EqlExecuteExtender.class).spliterator(), false)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
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
