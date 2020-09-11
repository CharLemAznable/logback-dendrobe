package com.github.charlemaznable.logback.miner.appender;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConsoleAppenderBatchTest {

    public final int TIMES = 1000;

    @SneakyThrows
    public void batchRun(int times) {
        for (int i = 0; i < times; ++i) {
            await().pollDelay(ofMillis(10)).until(() -> true);
        }
    }

    @SneakyThrows
    public void batchRunLog(int times) {
        for (int i = 0; i < times; ++i) {
            await().pollDelay(ofMillis(10)).until(() -> true);
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
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[ADDITIVITY]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[LEVEL]=INFO\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[CONSOLE.CHARSET]=utf-8\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[CONSOLE.PATTERN]=%5level %message%n\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[CONSOLE.TARGET]=batch\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[CONSOLE.IMMEDIATEFLUSH]=true\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[DQL.LEVEL]=OFF\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[DQL.CONNECTION]=ERROR\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[VERTX.LEVEL]=OFF\n" +
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderBatchTest[VERTX.NAME]=ERROR\n");
        await().forever().until(future::isDone);

        val threadCount = getRuntime().availableProcessors() + 1;

        val startTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRun(TIMES));
        val batchRunTime = currentTimeMillis() - startTime;

        val startLogTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRunLog(TIMES));
        val batchRunLogTime = currentTimeMillis() - startLogTime;

        assertTrue(batchRunLogTime < batchRunTime * 1.05); // 性能损耗小于5%

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }
}
