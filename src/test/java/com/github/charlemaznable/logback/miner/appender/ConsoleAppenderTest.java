package com.github.charlemaznable.logback.miner.appender;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConsoleAppenderTest {

    @Test
    public void testConsoleAppender() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "context.property.miner=test\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.LEVEL=INFO\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-CHARSET=utf-8\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%property{miner} %5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=miner-config\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-IMMEDIATEFLUSH=true\n");
        await().forever().until(future::isDone);

        assertEquals("Logger[com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest]", log.toString());

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

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testLoggerTrace() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-LEVEL=TRACE\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=test-trace\n");
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
                "\tat com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.testLoggerTrace(ConsoleAppenderTest.java:71)\n"));

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testLoggerDebug() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-LEVEL=debug\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=test-debug\n");
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
                "\tat com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.testLoggerDebug(ConsoleAppenderTest.java:109)\n"));

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testLoggerInfo() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-LEVEL=info\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=test-info\n");
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
                "\tat com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.testLoggerInfo(ConsoleAppenderTest.java:149)\n"));

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testLoggerWarn() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-LEVEL=warn\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=test-warn\n");
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
                "\tat com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.testLoggerWarn(ConsoleAppenderTest.java:191)\n"));

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testLoggerError() {
        ConsoleTarget.setUpMockConsole();
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test",
                "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-LEVEL=error\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.ADDITIVITY=no\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-PATTERN=%5level %message%n\n" +
                        "com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.CONSOLE-TARGET=test-error\n");
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
                "\tat com.github.charlemaznable.logback.miner.appender.ConsoleAppenderTest.testLoggerError(ConsoleAppenderTest.java:235)\n"));

        MockDiamondServer.tearDownMockServer();
        ConsoleTarget.tearDownMockConsole();
    }
}
