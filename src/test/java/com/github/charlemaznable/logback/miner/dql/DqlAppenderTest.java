package com.github.charlemaznable.logback.miner.dql;

import com.github.bingoohuang.westid.WestId;
import com.github.charlemaznable.logback.miner.appender.AnnotatedLog;
import com.github.charlemaznable.logback.miner.appender.ErrorLog;
import com.github.charlemaznable.logback.miner.appender.NotLog;
import com.github.charlemaznable.logback.miner.appender.RollSimpleLog;
import com.github.charlemaznable.logback.miner.appender.RollSqlLog;
import com.github.charlemaznable.logback.miner.appender.SimpleLog;
import com.github.charlemaznable.logback.miner.appender.SqlLog;
import com.github.charlemaznable.logback.miner.appender.SqlLogEx;
import com.github.charlemaznable.logback.miner.appender.SqlLogEx2;
import com.github.charlemaznable.logback.miner.appender.SqlLogMtcp;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.mtcp.MtcpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("Duplicates")
@Slf4j
public class DqlAppenderTest {

    private static final String CLASS_NAME = DqlAppenderTest.class.getName();

    private static final String DB0 = "db0";
    private static final String DB1 = "db1";
    private static final String DB_MTCP = "db.mtcp";
    private static final String CREATE_TABLE_SIMPLE_LOG = "" +
            "create table `simple_log` (" +
            "  `log_id` bigint not null," +
            "  `log_content` text," +
            "  `log_date` datetime(3)," +
            "  `log_date_time` datetime(3)," +
            "  primary key (`log_id`)" +
            ");\n";
    private static final String SELECT_SIMPLE_LOGS = "" +
            "select log_id, log_content, log_date, log_date_time from simple_log order by log_id";
    private static final String SELECT_SIMPLE_LOG_BY_ID = "" +
            "select log_id, log_content, log_date, log_date_time from simple_log where log_id = ##";

    private static Logger root;
    private static Logger self;

    private static final DockerImageName mysqlImageName = DockerImageName.parse("mysql:5.7.34");
    private static MySQLContainer mysql0 = new MySQLContainer<>(mysqlImageName).withDatabaseName(DB0);
    private static MySQLContainer mysql1 = new MySQLContainer<>(mysqlImageName).withDatabaseName(DB1);

    @BeforeAll
    public static void beforeAll() {
        mysql0.start();
        mysql1.start();

        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        Object diamondRemoteChecker = DiamondSubscriber.getInstance().getDiamondRemoteChecker();
        await().forever().until(() -> 1 <= on(diamondRemoteChecker)
                .field("diamondAllListener").field("allListeners").call("size").<Integer>get());
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("EqlConfig", DB0, "" +
                "driver=com.mysql.cj.jdbc.Driver\n" +
                "url=" + mysql0.getJdbcUrl() + "\n" +
                "user=" + mysql0.getUsername() + "\n" +
                "password=" + mysql0.getPassword() + "\n");
        MockDiamondServer.setConfigInfo("EqlConfig", DB1, "" +
                "driver=com.mysql.cj.jdbc.Driver\n" +
                "url=" + mysql1.getJdbcUrl() + "\n" +
                "user=" + mysql1.getUsername() + "\n" +
                "password=" + mysql1.getPassword() + "\n");
        MockDiamondServer.setConfigInfo("EqlConfig", DB_MTCP, "" +
                "driver=com.mysql.cj.jdbc.Driver\n" +
                "url=" + mysql0.getJdbcUrl() + "\n" +
                "user=" + mysql0.getUsername() + "\n" +
                "password=" + mysql0.getPassword() + "\n" +
                "connection.impl=com.github.charlemaznable.logback.miner.appender.MtcpAssertConnection\n");

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
        mysql0.stop();
        mysql1.stop();
    }

    @Test
    public void testDqlAppender() {
        val sql = "insert into simple_log (log_id,log_content,log_date,log_date_time) values(#event.westId#,concat('(', #property.miner#, '|', ifnull(#mdc.tenantId#, ''), '|', ifnull(#mdc.tenantCode#, ''), ')', #event.message#, #event.exception#),current_timestamp(),current_timestamp())";
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.property[miner]=test\n" +
                "root[dql.level]=info\n" +
                "root[dql.connection]=\n" +
                CLASS_NAME + "[appenders]=[dql]\n" +
                CLASS_NAME + "[dql.level]=info\n" +
                CLASS_NAME + "[dql.connection]=" + DB0 + "\n" +
                CLASS_NAME + "[dql.sql]=" + sql + "\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[vertx.level]=off\n");
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

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3)).until(() -> {
            List<Object> simpleLogs = new Dql(DB0).execute(SELECT_SIMPLE_LOGS);
            return 4 == simpleLogs.size();
        });

        List<SimpleLog> simpleLogs = new Dql(DB0).returnType(SimpleLog.class)
                .execute(SELECT_SIMPLE_LOGS);

        val querySimpleLog = simpleLogs.get(0);
        assertEquals(simpleLog.getLogId(), querySimpleLog.getLogId());
        assertEquals(simpleLog.getLogContent(), querySimpleLog.getLogContent());
        assertEquals(simpleLog.getLogDate(), querySimpleLog.getLogDate());
        assertEquals(simpleLog.getLogDateTime(), querySimpleLog.getLogDateTime());

        val queryNoDbLog = simpleLogs.get(1);
        assertEquals("(test||)no db log", queryNoDbLog.getLogContent());

        val queryNoDbLogNull = simpleLogs.get(2);
        assertEquals("(test||)no db log null: null", queryNoDbLogNull.getLogContent());

        val queryNoDbLogNotLog = simpleLogs.get(3);
        assertEquals("(test||)no db log not log: " + notLog.toString(), queryNoDbLogNotLog.getLogContent());

        val annotatedLog = new AnnotatedLog();
        annotatedLog.setALogId("1001");
        annotatedLog.setALogContent("annotated log");
        annotatedLog.setALogDate(new Date());
        annotatedLog.setALogDateTime(DateTime.now());
        self.info("annotated log: {}", annotatedLog);

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB1).limit(1).params("1001").execute(SELECT_SIMPLE_LOG_BY_ID)));

        SimpleLog queryAnnotatedLog = new Dql(DB1).limit(1).returnType(SimpleLog.class)
                .params("1001").execute(SELECT_SIMPLE_LOG_BY_ID);
        assertEquals(annotatedLog.getALogId(), queryAnnotatedLog.getLogId());
        assertEquals(annotatedLog.getALogContent(), queryAnnotatedLog.getLogContent());
        assertEquals(annotatedLog.getALogDate(), queryAnnotatedLog.getLogDate());
        assertEquals(annotatedLog.getALogDateTime(), queryAnnotatedLog.getLogDateTime());

        MtcpContext.setTenantId("testTenantId");
        MtcpContext.setTenantCode("testTenantCode");

        val sqlLog = new SqlLog();
        sqlLog.setLogId("1002");
        self.info("sql log: {}", sqlLog);

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB0).limit(1).params("1002").execute(SELECT_SIMPLE_LOG_BY_ID)));

        SqlLog querySqlLog = new Dql(DB0).limit(1).returnType(SqlLog.class)
                .params("1002").execute(SELECT_SIMPLE_LOG_BY_ID);
        assertEquals("(test|testTenantId|testTenantCode)" +
                "sql log: SqlLog(logId=1002, logContent=null)", querySqlLog.getLogContent());

        val sqlLogEx = new SqlLogEx();
        sqlLogEx.setLogId("1003");
        self.info("sql log exception: {}", sqlLogEx, new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB0).limit(1).params("1003").execute(SELECT_SIMPLE_LOG_BY_ID)));

        SqlLog querySqlLogEx = new Dql(DB0).limit(1).returnType(SqlLog.class)
                .params("1003").execute(SELECT_SIMPLE_LOG_BY_ID);
        assertTrue(querySqlLogEx.getLogContent().startsWith("" +
                "(test|testTenantId|testTenantCode)" +
                "sql log exception: SqlLogEx(logId=1003, logContent=null)" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testDqlAppender"));

        val sqlLogEx2 = new SqlLogEx2();
        sqlLogEx2.setLogId("1004");
        self.info("sql log exception: {}", sqlLogEx2, new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB0).limit(1).params("1004").execute(SELECT_SIMPLE_LOG_BY_ID)));

        SqlLog querySqlLogEx2 = new Dql(DB0).limit(1).returnType(SqlLog.class)
                .params("1004").execute(SELECT_SIMPLE_LOG_BY_ID);
        assertTrue(querySqlLogEx2.getLogContent().startsWith("" +
                "(test|testTenantId|testTenantCode)" +
                "sql log exception: SqlLogEx2(logId=1004, logContent=null)" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testDqlAppender"));

        val sqlLogMtcp = new SqlLogMtcp();
        sqlLogMtcp.setLogId("1005");
        self.info("sql log: {}", sqlLogMtcp);

        await().pollDelay(Duration.ofSeconds(3)).until(() ->
                nonNull(new Dql(DB0).limit(1).params("1005").execute(SELECT_SIMPLE_LOG_BY_ID)));

        SqlLog querySqlLogMtcp = new Dql(DB0).limit(1).returnType(SqlLog.class)
                .params("1005").execute(SELECT_SIMPLE_LOG_BY_ID);
        assertEquals("(test|testTenantId|testTenantCode)" +
                "sql log: SqlLogMtcp(logId=1005, logContent=null)", querySqlLogMtcp.getLogContent());

        MtcpContext.clearTenant();
    }

    @Test
    public void testDqlAppenderRolling() {
        val sql = "insert into $activeTableName$ (log_id,log_content,log_date) values(#event.westId#,#event.message#,current_timestamp())";
        val tableNamePattern = "d_rolling_log_%d{yyyyMMddHHmmss}";
        val prepareSql = "" +
                "create table $activeTableName$ (" +
                "  `log_id` bigint not null," +
                "  `log_content` text," +
                "  `log_date` datetime(3)," +
                "  primary key (`log_id`)" +
                ");\n";
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[appenders]=[dql]\n" +
                CLASS_NAME + "[dql.level]=info\n" +
                CLASS_NAME + "[dql.connection]=" + DB0 + "\n" +
                CLASS_NAME + "[dql.sql]=" + sql + "\n" +
                CLASS_NAME + "[dql.tableNamePattern]=" + tableNamePattern + "\n" +
                CLASS_NAME + "[dql.prepareSql]=" + prepareSql);
        await().forever().until(future::isDone);

        val expectBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            self.trace("trace logging");
            self.debug("debug logging");
            self.info("info logging");
            self.warn("warn logging");
            self.error("error logging");
            expectBuilder.append("info logging\n" +
                    "warn logging\n" +
                    "error logging\n");
            await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
        }

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3)).until(() -> {
            List<String> dTableNames = new Dql(DB0).execute("" +
                    "select table_name from information_schema.tables" +
                    " where table_name like 'd_rolling_log_%'" +
                    " order by table_name");
            return 3 == dTableNames.size();
        });

        List<String> dTableNames = new Dql(DB0).execute("" +
                "select table_name from information_schema.tables" +
                " where table_name like 'd_rolling_log_%'" +
                " order by table_name");
        val defaultLogBuilder = new StringBuilder();
        for (val dTableName : dTableNames) {
            List<String> logContents = new Dql(DB0).execute("" +
                    "select log_content from " + dTableName +
                    " order by log_id");
            for (val logContent : logContents) {
                defaultLogBuilder.append(logContent).append("\n");
            }
        }
        assertEquals(expectBuilder.toString(), defaultLogBuilder.toString());

        expectBuilder.setLength(0);
        for (int i = 0; i < 3; i++) {
            self.trace("{}", new RollSqlLog("bean trace logging"));
            self.debug("{}", new RollSqlLog("bean debug logging"));
            self.info("{}", new RollSqlLog("bean info logging"));
            self.warn("{}", new RollSqlLog("bean warn logging"));
            self.error("{}", new RollSqlLog("bean error logging"));
            expectBuilder.append("bean info logging\n" +
                    "bean warn logging\n" +
                    "bean error logging\n");
            await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
        }

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3)).until(() -> {
            List<String> bTableNames = new Dql(DB0).execute("" +
                    "select table_name from information_schema.tables" +
                    " where table_name like 'b_rolling_log_%'" +
                    " order by table_name");
            return 3 == bTableNames.size();
        });

        List<String> bTableNames = new Dql(DB0).execute("" +
                "select table_name from information_schema.tables" +
                " where table_name like 'b_rolling_log_%'" +
                " order by table_name");
        val beanLogBuilder = new StringBuilder();
        for (val tableName : bTableNames) {
            List<String> logContents = new Dql(DB0).execute("" +
                    "select log_content from " + tableName +
                    " order by log_id");
            for (val logContent : logContents) {
                beanLogBuilder.append(logContent).append("\n");
            }
        }
        assertEquals(expectBuilder.toString(), beanLogBuilder.toString());

        expectBuilder.setLength(0);
        for (int i = 0; i < 3; i++) {
            val id1 = Objects.toString(WestId.next());
            self.trace("{}", new RollSimpleLog(id1, "simple trace logging"));
            val id2 = Objects.toString(WestId.next());
            self.debug("{}", new RollSimpleLog(id2, "simple debug logging"));
            val id3 = Objects.toString(WestId.next());
            self.info("{}", new RollSimpleLog(id3, "simple info logging"));
            val id4 = Objects.toString(WestId.next());
            self.warn("{}", new RollSimpleLog(id4, "simple warn logging"));
            val id5 = Objects.toString(WestId.next());
            self.error("{}", new RollSimpleLog(id5, "simple error logging"));
            expectBuilder.append(id3).append(":").append("simple info logging\n");
            expectBuilder.append(id4).append(":").append("simple warn logging\n");
            expectBuilder.append(id5).append(":").append("simple error logging\n");
            await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
        }

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3)).until(() -> {
            List<String> sTableNames = new Dql(DB0).execute("" +
                    "select table_name from information_schema.tables" +
                    " where table_name like 's_rolling_log_%'" +
                    " order by table_name");
            return 3 == sTableNames.size();
        });

        List<String> sTableNames = new Dql(DB0).execute("" +
                "select table_name from information_schema.tables" +
                " where table_name like 's_rolling_log_%'" +
                " order by table_name");
        val simpleLogBuilder = new StringBuilder();
        for (val tableName : sTableNames) {
            List<String> logContents = new Dql(DB0).execute("" +
                    "select concat(log_id, ':', log_content) from " + tableName +
                    " order by log_id");
            for (val logContent : logContents) {
                simpleLogBuilder.append(logContent).append("\n");
            }
        }
        assertEquals(expectBuilder.toString(), simpleLogBuilder.toString());
    }
}