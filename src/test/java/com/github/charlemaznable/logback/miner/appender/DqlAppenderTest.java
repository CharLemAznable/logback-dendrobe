package com.github.charlemaznable.logback.miner.appender;

import lombok.val;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.eql.diamond.Dql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DqlAppenderTest {

    private static final String DB0 = "db0";
    private static final String DB1 = "db1";
    private static final String CREATE_TABLE_SIMPLE_LOG = "CREATE TABLE `SIMPLE_LOG` (" +
            "  `LOG_ID` BIGINT NOT NULL," +
            "  `LOG_CONTENT` TEXT," +
            "  `LOG_DATE` DATETIME," +
            "  `LOG_DATE_TIME` DATETIME," +
            "  PRIMARY KEY (`LOG_ID`)" +
            ");\n";
    private static final String SELECT_SIMPLE_LOG = "" +
            "SELECT LOG_ID, LOG_CONTENT, LOG_DATE, LOG_DATE_TIME FROM SIMPLE_LOG WHERE LOG_ID = ##";

    private static Logger root;
    private static Logger self;

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("EqlConfig", DB0,
                "driver=org.h2.Driver\n" +
                        "url=jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE\n" +
                        "user=\n" +
                        "password=\n");
        MockDiamondServer.setConfigInfo("EqlConfig", DB1,
                "driver=org.h2.Driver\n" +
                        "url=jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE\n" +
                        "user=\n" +
                        "password=\n");

        new Dql(DB0).execute("" +
                CREATE_TABLE_SIMPLE_LOG);

        new Dql(DB1).execute("" +
                CREATE_TABLE_SIMPLE_LOG);

        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        self = LoggerFactory.getLogger(DqlAppenderTest.class);
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testDqlAppender() {
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "context.property[miner]=test\n" +
                        "com.github.charlemaznable.logback.miner.appender.DqlAppenderTest[dql.level]=info\n" +
                        "com.github.charlemaznable.logback.miner.appender.DqlAppenderTest[dql.connection]=" + DB0 + "\n" +
                        "com.github.charlemaznable.logback.miner.appender.DqlAppenderTest[console.level]=off");
        await().forever().until(future::isDone);

        root.info("no db log");
        self.info("no db log");

        root.info("no db log null: {}", (Object) null);
        self.info("no db log null: {}", (Object) null);

        val notLog = new NotLog();
        notLog.setLogId("1000");
        notLog.setLogContent("no db log not log");
        root.info("no db log not log: {}", notLog);
        self.info("no db log not log: {}", notLog);

        val emptyLog = new EmptyLog();
        emptyLog.setLogId("1000");
        emptyLog.setLogContent("no db log empty log");
        root.info("no db log empty log: {}", emptyLog);
        self.info("no db log empty log: {}", emptyLog);

        val errorLog = new ErrorLog();
        errorLog.setLogId("1000");
        errorLog.setLogContent("no db log error log");
        root.info("no db log error log: {}", errorLog);
        self.info("no db log error log: {}", errorLog);

        val simpleLog = new SimpleLog();
        simpleLog.setLogId("1000");
        simpleLog.setLogContent("simple log");
        simpleLog.setLogDate(new Date());
        simpleLog.setLogDateTime(DateTime.now());
        root.info("simple log: {} >> actual ignored", simpleLog);
        self.info("simple log: {}", simpleLog);

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB0).limit(1).params("1000").execute(SELECT_SIMPLE_LOG)));

        SimpleLog queryLog = new Dql(DB0).limit(1).returnType(SimpleLog.class)
                .params("1000").execute(SELECT_SIMPLE_LOG);
        assertEquals(simpleLog.getLogId(), queryLog.getLogId());
        assertEquals(simpleLog.getLogContent(), queryLog.getLogContent());
        assertEquals(simpleLog.getLogDate(), queryLog.getLogDate());
        assertEquals(simpleLog.getLogDateTime(), queryLog.getLogDateTime());
    }
}
