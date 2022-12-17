package com.github.charlemaznable.logback.dendrobe.eql;

import com.github.bingoohuang.westid.WestId;
import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.Util;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static com.github.charlemaznable.logback.dendrobe.eql.TestEqlConfigService.setConfig;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
public class EqlAppenderBatchTest {

    private static final String CLASS_NAME = EqlAppenderBatchTest.class.getName();
    private static final int TIMES = 1000;
    private static final String DBBatch = "db_batch";
    private static final String CREATE_TABLE_SIMPLE_LOG = "" +
            "create table `simple_log` (" +
            "  `log_id` bigint not null," +
            "  `log_content` text," +
            "  `log_date` datetime(3)," +
            "  primary key (`log_id`)" +
            ");\n";
    private static final DockerImageName mysqlImageName = DockerImageName.parse("mysql:5.7.34");
    private static final MySQLContainer<?> mysql = new MySQLContainer<>(mysqlImageName).withDatabaseName(DBBatch);

    @BeforeAll
    public static void beforeAll() {
        mysql.start();

        setConfig(DBBatch, "" +
                "driver=com.mysql.cj.jdbc.Driver\n" +
                "url=" + mysql.getJdbcUrl() + "\n" +
                "user=" + mysql.getUsername() + "\n" +
                "password=" + mysql.getPassword() + "\n");

        new TestEql(DBBatch).execute(CREATE_TABLE_SIMPLE_LOG);
    }

    @AfterAll
    public static void afterAll() {
        mysql.stop();
    }

    public void batchRun(int times) {
        for (int i = 0; i < times; ++i) {
            awaitForMillis(10);
            val simpleLog = new SimpleLog();
            simpleLog.setLogId(Long.toString(WestId.next()));
            simpleLog.setLogContent("simple log");
            simpleLog.setLogDate(new Date());
        }
    }

    public void batchRunLog(int times) {
        for (int i = 0; i < times; ++i) {
            awaitForMillis(10);
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
    public void testEqlAppenderBatch() {
        listener().reset(parseStringToProperties("" +
                "eql[console.level]=off\norg.n3r.eql[console.level]=off\n" +
                CLASS_NAME + "[level]=info\n" +
                CLASS_NAME + "[eql.connection]=" + DBBatch + "\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[console.target]=error\n" +
                CLASS_NAME + "[vertx.level]=off\n" +
                CLASS_NAME + "[vertx.name]=error\n"));

        val threadCount = getRuntime().availableProcessors() + 1;

        val startTime = currentTimeMillis();
        assertDoesNotThrow(() -> routineRun(threadCount, () -> batchRun(TIMES)));
        val batchRunTime = currentTimeMillis() - startTime;

        val startLogTime = currentTimeMillis();
        assertDoesNotThrow(() -> routineRun(threadCount, () -> batchRunLog(TIMES)));
        val batchRunLogTime = currentTimeMillis() - startLogTime;

        Util.report("Original time: " + batchRunTime + "ms, " +
                "logging time: " + batchRunLogTime + "ms, " +
                "rating: " + new BigDecimal(batchRunLogTime).divide(
                new BigDecimal(batchRunTime), 2, RoundingMode.HALF_UP));
    }

    @Getter
    @Setter
    @EqlLogBean
    public static class SimpleLog {

        private String logId;
        private String logContent;
        private Date logDate;
    }
}
