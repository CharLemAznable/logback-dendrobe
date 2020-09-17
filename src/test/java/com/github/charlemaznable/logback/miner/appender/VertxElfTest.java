package com.github.charlemaznable.logback.miner.appender;

import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.logback.miner.appender.VertxElf.buildVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertxQuietly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class VertxElfTest {

    @Test
    public void testVertxOptionsElf() {
        val vertx = buildVertx(new VertxOptions());
        assertFalse(vertx.isClustered());
        closeVertxQuietly(vertx);

        assertDoesNotThrow(() -> closeVertxQuietly(null));
        assertDoesNotThrow(() -> closeVertx(null));
    }
}
