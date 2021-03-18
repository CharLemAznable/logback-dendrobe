package com.github.charlemaznable.elf;

import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class Await {

    private Await() {
        throw new UnsupportedOperationException();
    }

    public static void awaitOfMicros(long duration) {
        await(duration, TimeUnit.MICROSECONDS);
    }

    public static void awaitOfMillis(long duration) {
        await(duration, TimeUnit.MILLISECONDS);
    }

    public static void awaitOfSeconds(long duration) {
        await(duration, TimeUnit.SECONDS);
    }

    public static void await(long duration, TimeUnit unit) {
        await(unit.toMillis(duration));
    }

    public static void await(long millis) {
        Awaitility.await().forever().pollDelay(
                Duration.ofMillis(millis)).until(() -> true);
    }
}
