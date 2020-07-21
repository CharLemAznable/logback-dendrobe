package com.github.charlemaznable.logback.miner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class LogbackMinerDiamondListenerTest {

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
    }

    @Test
    public void testLogbackMinerDiamondListenerRaw() {
        MockDiamondServer.setUpMockServer();

        val diamondListener = new LogbackMinerDiamondListener();

        val future1 = MockDiamondServer.updateDiamond(
                "Logback", "test", "key1=value1\n");
        await().forever().until(future1::isDone);
        assertEquals("value1", diamondListener.getRaw("key1"));
        assertNull(diamondListener.getRaw("key2"));

        val future2 = MockDiamondServer.updateDiamond(
                "Logback", "test", "key2=value2\n");
        await().forever().until(future2::isDone);
        assertNull(diamondListener.getRaw("key1"));
        assertEquals("value2", diamondListener.getRaw("key2"));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testLogbackMinerDiamondListener() {
        MockDiamondServer.setUpMockServer();

        val rootLogger = (Logger) LoggerFactory.getLogger("root");
        val thisLogger = (Logger) log;

        val future1 = MockDiamondServer.updateDiamond(
                "Logback", "test", "ROOT[LEVEL]=INFO\n" +
                        "ROOT[ADDITIVITY]=true\n" +
                        "ROOT[CONSOLE.LEVEL]=DEBUG\n");
        await().forever().until(future1::isDone);
        assertEquals(Level.INFO, rootLogger.getLevel());
        assertTrue(rootLogger.isAdditive());
        assertTrue(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertNull(thisLogger.getLevel());
        assertEquals(Level.INFO, thisLogger.getEffectiveLevel());
        assertTrue(thisLogger.isAdditive());
        assertTrue(thisLogger.isDebugEnabled()); // decide by turbo filter

        val future2 = MockDiamondServer.updateDiamond(
                "Logback", "test", "ROOT[LEVEL]=warn\n" +
                        "ROOT[ADDITIVITY]=false\n" +
                        "ROOT[CONSOLE.LEVEL]=info\n");
        await().forever().until(future2::isDone);
        assertEquals(Level.WARN, rootLogger.getLevel());
        assertFalse(rootLogger.isAdditive());
        assertFalse(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(rootLogger.isInfoEnabled()); // decide by turbo filter
        assertNull(thisLogger.getLevel());
        assertEquals(Level.WARN, thisLogger.getEffectiveLevel());
        assertTrue(thisLogger.isAdditive());
        assertFalse(thisLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(thisLogger.isInfoEnabled()); // decide by turbo filter

        val future3 = MockDiamondServer.updateDiamond(
                "Logback", "test", "root[level]=warn\n" +
                        "root[additivity]=no\n" +
                        "root[console.level]=info\n" +
                        this.getClass().getName() + "[level]=debug\n" +
                        this.getClass().getName() + "[additivity]=off\n" +
                        this.getClass().getName() + "[console.level]=debug\n");
        await().forever().until(future3::isDone);
        assertEquals(Level.WARN, rootLogger.getLevel());
        assertFalse(rootLogger.isAdditive());
        assertFalse(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(rootLogger.isInfoEnabled()); // decide by turbo filter
        assertEquals(Level.DEBUG, thisLogger.getLevel());
        assertEquals(Level.DEBUG, thisLogger.getEffectiveLevel());
        assertFalse(thisLogger.isAdditive());
        assertTrue(thisLogger.isDebugEnabled()); // decide by turbo filter

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testLogbackMinerDiamondListenerLoggerContext() {
        MockDiamondServer.setUpMockServer();

        val loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        val future1 = MockDiamondServer.updateDiamond(
                "Logback", "test", "context.packagingDataEnabled=Y\n" +
                        "context.maxCallerDataDepth=4\n" +
                        "context.frameworkPackages=com.github.charlemaznable.logback.miner\n" +
                        "context.property[miner]=test\n" +
                        "root[console.target]=System.warn");
        await().forever().until(future1::isDone);
        assertTrue(loggerContext.isPackagingDataEnabled());
        assertEquals(4, loggerContext.getMaxCallerDataDepth());
        assertEquals("com.github.charlemaznable.logback.miner", loggerContext.getFrameworkPackages().get(0));
        assertEquals("test", loggerContext.getProperty("miner"));

        val future2 = MockDiamondServer.updateDiamond(
                "Logback", "test", "\n");
        await().forever().until(future2::isDone);
        assertFalse(loggerContext.isPackagingDataEnabled());
        assertEquals(8, loggerContext.getMaxCallerDataDepth());
        assertTrue(loggerContext.getFrameworkPackages().isEmpty());
        assertNull(loggerContext.getProperty("miner"));

        MockDiamondServer.tearDownMockServer();
    }
}
