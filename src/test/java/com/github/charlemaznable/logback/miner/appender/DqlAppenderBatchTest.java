package com.github.charlemaznable.logback.miner.appender;

import com.github.bingoohuang.westid.WestId;
import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.eql.diamond.Dql;

import java.util.Date;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DqlAppenderBatchTest {

    public final int TIMES = 1000;

    private static final String DBBatch = "db_batch";
    private static final String CREATE_TABLE_SIMPLE_LOG = "" +
            "CREATE TABLE `SIMPLE_LOG` (" +
            "  `LOG_ID` BIGINT NOT NULL," +
            "  `LOG_CONTENT` TEXT," +
            "  `LOG_DATE` DATETIME," +
            "  PRIMARY KEY (`LOG_ID`)" +
            ");\n";

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("EqlConfig", DBBatch, "" +
                "driver=org.h2.Driver\n" +
                "url=jdbc:h2:mem:db_batch;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE\n" +
                "user=\n" +
                "password=\n");

        new Dql(DBBatch).execute("" +
                CREATE_TABLE_SIMPLE_LOG);
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

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
            val simpleLog = new SimpleLog();
            simpleLog.setLogId(Long.toString(WestId.next()));
            simpleLog.setLogContent("simple log");
            simpleLog.setLogDate(new Date());
            log.info("simple log: {}", simpleLog);
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
    public void testDqlAppenderBatch() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[appender]=dql\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[dql.connection]=" + DBBatch + "\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[console.target]=error\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[vertx.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.DqlAppenderBatchTest[vertx.name]=error\n");
        await().forever().until(future::isDone);

        val threadCount = getRuntime().availableProcessors() + 1;

        val startTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRun(TIMES));
        val batchRunTime = currentTimeMillis() - startTime;

        val startLogTime = currentTimeMillis();
        routineRun(threadCount, () -> batchRunLog(TIMES));
        val batchRunLogTime = currentTimeMillis() - startLogTime;

        assertTrue(batchRunLogTime < batchRunTime * 1.1); // 性能损耗小于10%
    }

    @Getter
    @Setter
    @LogbackBean
    public static class SimpleLog {

        private String logId;
        private String logContent;
        private Date logDate;
    }
}
