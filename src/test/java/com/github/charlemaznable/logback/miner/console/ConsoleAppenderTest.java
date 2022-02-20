package com.github.charlemaznable.logback.miner.console;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.time.Duration;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConsoleAppenderTest {

    private static final String CLASS_NAME = ConsoleAppenderTest.class.getName();

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        Object diamondRemoteChecker = DiamondSubscriber.getInstance().getDiamondRemoteChecker();
        await().forever().until(() -> 1 <= on(diamondRemoteChecker)
                .field("diamondAllListener").field("allListeners").call("size").<Integer>get());
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testConsoleAppender() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.maxCallerDataDepth=abc\ncontext.property[miner]=test\n" +
                CLASS_NAME + "[LEVEL]=INFO\n" +
                CLASS_NAME + "[APPENDERS]=[CONSOLE]\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%property{miner} %5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=miner-config\n" +
                CLASS_NAME + "[CONSOLE.IMMEDIATEFLUSH]=true\n");
        await().forever().until(future::isDone);

        assertEquals("Logger[" + CLASS_NAME + "]", log.toString());

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging");

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("miner-config");
        assertNotNull(outputBuffer);
        assertEquals("test  INFO info logging\n" +
                        "test  WARN warn logging\n" +
                        "test ERROR error logging\n",
                outputBuffer.output());
    }

    @Test
    public void testLoggerTrace() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=TRACE\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf-8\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-trace\n");
        await().forever().until(future::isDone);

        assertTrue(log.isTraceEnabled());

        log.trace("trace0");
        log.trace("trace1: {}", "single");
        log.trace("trace2: {}, {}", "double1", "double2");
        log.trace("trace3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.trace("traceE:", new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("test-trace");
        assertNotNull(outputBuffer);
        assertTrue(outputBuffer.output().startsWith("" +
                "TRACE trace0\n" +
                "TRACE trace1: single\n" +
                "TRACE trace2: double1, double2\n" +
                "TRACE trace3: multi1, multi2, multi3\n" +
                "TRACE traceE:\n" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testLoggerTrace(ConsoleAppenderTest.java:"));
    }

    @Test
    public void testLoggerDebug() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[DQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=debug\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-debug\n");
        await().forever().until(future::isDone);

        assertFalse(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());

        log.trace("trace0");
        log.debug("debug0");
        log.debug("debug1: {}", "single");
        log.debug("debug2: {}, {}", "double1", "double2");
        log.debug("debug3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.debug("debugE:", new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("test-debug");
        assertNotNull(outputBuffer);
        assertTrue(outputBuffer.output().startsWith("" +
                "DEBUG debug0\n" +
                "DEBUG debug1: single\n" +
                "DEBUG debug2: double1, double2\n" +
                "DEBUG debug3: multi1, multi2, multi3\n" +
                "DEBUG debugE:\n" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testLoggerDebug(ConsoleAppenderTest.java:"));
    }

    @Test
    public void testLoggerInfo() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[DQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=info\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-info\n");
        await().forever().until(future::isDone);

        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());

        log.trace("trace0");
        log.debug("debug0");
        log.info("info0");
        log.info("info1: {}", "single");
        log.info("info2: {}, {}", "double1", "double2");
        log.info("info3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.info("infoE:", new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("test-info");
        assertNotNull(outputBuffer);
        assertTrue(outputBuffer.output().startsWith("" +
                " INFO info0\n" +
                " INFO info1: single\n" +
                " INFO info2: double1, double2\n" +
                " INFO info3: multi1, multi2, multi3\n" +
                " INFO infoE:\n" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testLoggerInfo(ConsoleAppenderTest.java:"));
    }

    @Test
    public void testLoggerWarn() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[DQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=warn\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-warn\n");
        await().forever().until(future::isDone);

        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertFalse(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());

        log.trace("trace0");
        log.debug("debug0");
        log.info("info0");
        log.warn("warn0");
        log.warn("warn1: {}", "single");
        log.warn("warn2: {}, {}", "double1", "double2");
        log.warn("warn3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.warn("warnE:", new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("test-warn");
        assertNotNull(outputBuffer);
        assertTrue(outputBuffer.output().startsWith("" +
                " WARN warn0\n" +
                " WARN warn1: single\n" +
                " WARN warn2: double1, double2\n" +
                " WARN warn3: multi1, multi2, multi3\n" +
                " WARN warnE:\n" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testLoggerWarn(ConsoleAppenderTest.java:"));
    }

    @Test
    public void testLoggerError() {
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                CLASS_NAME + "[DQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=error\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-error\n");
        await().forever().until(future::isDone);

        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertFalse(log.isInfoEnabled());
        assertFalse(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());

        log.trace("trace0");
        log.debug("debug0");
        log.info("info0");
        log.warn("warn0");
        log.error("error0");
        log.error("error1: {}", "single");
        log.error("error2: {}, {}", "double1", "double2");
        log.error("error3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.error("errorE:", new Exception("exception"));

        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val outputBuffer = ConsoleTarget.mockBufferByName("test-error");
        assertNotNull(outputBuffer);
        assertTrue(outputBuffer.output().startsWith("" +
                "ERROR error0\n" +
                "ERROR error1: single\n" +
                "ERROR error2: double1, double2\n" +
                "ERROR error3: multi1, multi2, multi3\n" +
                "ERROR errorE:\n" +
                "java.lang.Exception: exception\n" +
                "\tat " + CLASS_NAME + ".testLoggerError(ConsoleAppenderTest.java:"));
    }
}
