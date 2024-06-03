package com.github.charlemaznable.logback.dendrobe.console;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.Reporter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
public class ConsoleAppenderBatchTest {

    private static final String CLASS_NAME = ConsoleAppenderBatchTest.class.getName();

    public final int TIMES = 1000;

    public void batchRun(int times) {
        for (int i = 0; i < times; ++i) {
            awaitForMillis(10);
        }
    }

    public void batchRunLog(int times) {
        for (int i = 0; i < times; ++i) {
            awaitForMillis(10);
            log.info("message");
        }
    }

    @SneakyThrows
    public void routineRun(int threadCount, Runnable target) {
        val threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(target);
            threads[i].start();
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
    }

    @Test
    public void testConsoleAppenderBatch() {
        ConsoleTarget.setUpMockConsole();
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[LEVEL]=INFO\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf-8\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=batch\n" +
                CLASS_NAME + "[CONSOLE.IMMEDIATEFLUSH]=true\n" +
                CLASS_NAME + "[EQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[EQL.CONNECTION]=ERROR\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.NAME]=ERROR\n"));

        val threadCount = getRuntime().availableProcessors() + 1;

        val startTime = currentTimeMillis();
        assertDoesNotThrow(() -> routineRun(threadCount, () -> batchRun(TIMES)));
        val batchRunTime = currentTimeMillis() - startTime;

        val startLogTime = currentTimeMillis();
        assertDoesNotThrow(() -> routineRun(threadCount, () -> batchRunLog(TIMES)));
        val batchRunLogTime = currentTimeMillis() - startLogTime;

        Reporter.error("Original time: " + batchRunTime + "ms, " +
                "logging time: " + batchRunLogTime + "ms, " +
                "rating: " + new BigDecimal(batchRunLogTime).divide(
                new BigDecimal(batchRunTime), 2, RoundingMode.HALF_UP));

        ConsoleTarget.tearDownMockConsole();
    }
}
