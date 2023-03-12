package com.github.charlemaznable.logback.dendrobe.console;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConsoleAppenderTest {

    private static final String CLASS_NAME = ConsoleAppenderTest.class.getName();

    @BeforeAll
    public static void beforeAll() {
        ConsoleTarget.setUpMockConsole();
    }

    @AfterAll
    public static void afterAll() {
        ConsoleTarget.tearDownMockConsole();
    }

    @Test
    public void testConsoleAppender() {
        listener().reset(parseStringToProperties("" +
                "context.maxCallerDataDepth=abc\ncontext.property[dendrobe]=test\n" +
                CLASS_NAME + "[LEVEL]=INFO\n" +
                CLASS_NAME + "[APPENDERS]=[CONSOLE]\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%property{dendrobe} %5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=dendrobe-config\n" +
                CLASS_NAME + "[CONSOLE.IMMEDIATEFLUSH]=true\n"));

        assertEquals("Logger[" + CLASS_NAME + "]", log.toString());

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging");

        awaitForSeconds(3);

        val outputBuffer = ConsoleTarget.mockBufferByName("dendrobe-config");
        assertNotNull(outputBuffer);
        assertEquals("test  INFO info logging\n" +
                        "test  WARN warn logging\n" +
                        "test ERROR error logging\n",
                outputBuffer.output());
    }

    @Test
    public void testLoggerTrace() {
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=TRACE\n" +
                CLASS_NAME + "[CONSOLE.CHARSET]=utf-8\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-trace\n"));

        assertTrue(log.isTraceEnabled());

        log.trace("trace0");
        log.trace("trace1: {}", "single");
        log.trace("trace2: {}, {}", "double1", "double2");
        log.trace("trace3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.trace("traceE:", new Exception("exception"));

        awaitForSeconds(3);

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
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[EQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ES.LEVEL]=OFF\n" +
                CLASS_NAME + "[KAFKA.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=debug\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-debug\n"));

        assertFalse(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());

        log.trace("trace0");
        log.debug("debug0");
        log.debug("debug1: {}", "single");
        log.debug("debug2: {}, {}", "double1", "double2");
        log.debug("debug3: {}, {}, {}", "multi1", "multi2", "multi3");
        log.debug("debugE:", new Exception("exception"));

        awaitForSeconds(3);

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
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[EQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ES.LEVEL]=OFF\n" +
                CLASS_NAME + "[KAFKA.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=info\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-info\n"));

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

        awaitForSeconds(3);

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
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[EQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ES.LEVEL]=OFF\n" +
                CLASS_NAME + "[KAFKA.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=warn\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-warn\n"));

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

        awaitForSeconds(3);

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
        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[EQL.LEVEL]=OFF\n" +
                CLASS_NAME + "[VERTX.LEVEL]=OFF\n" +
                CLASS_NAME + "[FILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ROLLINGFILE.LEVEL]=OFF\n" +
                CLASS_NAME + "[ES.LEVEL]=OFF\n" +
                CLASS_NAME + "[KAFKA.LEVEL]=OFF\n" +
                CLASS_NAME + "[CONSOLE.LEVEL]=error\n" +
                CLASS_NAME + "[CONSOLE.PATTERN]=%5level %message%n\n" +
                CLASS_NAME + "[CONSOLE.TARGET]=test-error\n"));

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

        awaitForSeconds(3);

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
