package com.github.charlemaznable.logback.miner.console;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.helpers.Util;

import java.math.BigDecimal;

import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConsoleAppenderBatchTest {

    private static final String CLASS_NAME = ConsoleAppenderBatchTest.class.getName();

    public final int TIMES = 1000;

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        Object diamondRemoteChecker = DiamondSubscriber.getInstance().getDiamondRemoteChecker();
        await().forever().until(() -> 1 <= on(diamondRemoteChecker)
                .field("diamondAllListener").field("allListeners").call("size").<Integer>get());
    }

    @SneakyThrows
    public void batchRun(int times) {
        for (int i = 0; i < times; ++i) {
            awaitForMillis(10);
        }
    }

    @SneakyThrows
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
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[LEVEL]=INFO\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf-8\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=batch\n" +
                CLASS_NAME + "[CONSOLE.IMMEDIATEFLUSH]=true\n" +
                CLASS_NAME + "[DQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[DQL.CONNECTION]=ERROR\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.NAME]=ERROR\n");
        await().forever().until(future::isDone);

        val threadCount = getRuntime().availableProcessors() + 1;

        val startTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRun(TIMES));
        val batchRunTime = currentTimeMillis() - startTime;

        val startLogTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRunLog(TIMES));
        val batchRunLogTime = currentTimeMillis() - startLogTime;

        assertTrue(batchRunLogTime > batchRunTime);
        Util.report("Original time: " + batchRunTime + "ms, " +
                "logging time: " + batchRunLogTime + "ms, " +
                "rating: " + new BigDecimal(batchRunLogTime).divide(
                new BigDecimal(batchRunTime), 2, ROUND_HALF_UP).toString());

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }
}
