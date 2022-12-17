package com.github.charlemaznable.logback.dendrobe;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class LogbackDendrobeListenerTest {

    @Test
    public void testLogbackDendrobeListenerRaw() {
        listener().reset(parseStringToProperties("key1=value1\n"));
        assertEquals("value1", listener().getRaw("key1"));
        assertNull(listener().getRaw("key2"));

        listener().reset(parseStringToProperties("key2=value2\n"));
        assertNull(listener().getRaw("key1"));
        assertEquals("value2", listener().getRaw("key2"));
    }

    @Test
    public void testLogbackDendrobeListener() {
        val rootLogger = (Logger) LoggerFactory.getLogger("root");
        val thisLogger = (Logger) log;

        listener().reset(parseStringToProperties("""
                ROOT[LEVEL]=INFO
                ROOT[ADDITIVITY]=true
                ROOT[CONSOLE.LEVEL]=DEBUG
                """));
        assertEquals(Level.INFO, rootLogger.getLevel());
        assertTrue(rootLogger.isAdditive());
        assertTrue(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertNull(thisLogger.getLevel());
        assertEquals(Level.INFO, thisLogger.getEffectiveLevel());
        assertTrue(thisLogger.isAdditive());
        assertTrue(thisLogger.isDebugEnabled()); // decide by turbo filter

        listener().reset(parseStringToProperties("""
                ROOT[LEVEL]=warn
                ROOT[ADDITIVITY]=false
                ROOT[CONSOLE.LEVEL]=info
                """));
        assertEquals(Level.WARN, rootLogger.getLevel());
        assertFalse(rootLogger.isAdditive());
        assertFalse(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(rootLogger.isInfoEnabled()); // decide by turbo filter
        assertNull(thisLogger.getLevel());
        assertEquals(Level.WARN, thisLogger.getEffectiveLevel());
        assertTrue(thisLogger.isAdditive());
        assertFalse(thisLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(thisLogger.isInfoEnabled()); // decide by turbo filter

        listener().reset(parseStringToProperties("" +
                "root[level]=warn\n" +
                "root[additivity]=no\n" +
                "root[console.level]=info\n" +
                this.getClass().getName() + "[level]=debug\n" +
                this.getClass().getName() + "[additivity]=off\n" +
                this.getClass().getName() + "[console.level]=debug\n"));
        assertEquals(Level.WARN, rootLogger.getLevel());
        assertFalse(rootLogger.isAdditive());
        assertFalse(rootLogger.isDebugEnabled()); // decide by turbo filter
        assertTrue(rootLogger.isInfoEnabled()); // decide by turbo filter
        assertEquals(Level.DEBUG, thisLogger.getLevel());
        assertEquals(Level.DEBUG, thisLogger.getEffectiveLevel());
        assertFalse(thisLogger.isAdditive());
        assertTrue(thisLogger.isDebugEnabled()); // decide by turbo filter
    }

    @Test
    public void testLogbackDendrobeListenerLoggerContext() {
        val loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        listener().reset(parseStringToProperties("""
                context.packagingDataEnabled=Y
                context.maxCallerDataDepth=4
                context.frameworkPackages=com.github.charlemaznable.logback.dendrobe
                context.property[dendrobe]=test
                context.property[dendrobe2=test
                root[console.target]=System.warn"""));
        assertTrue(loggerContext.isPackagingDataEnabled());
        assertEquals(4, loggerContext.getMaxCallerDataDepth());
        assertEquals("com.github.charlemaznable.logback.dendrobe", loggerContext.getFrameworkPackages().get(0));
        assertEquals("test", loggerContext.getProperty("dendrobe"));
        assertNull(loggerContext.getProperty("dendrobe2"));

        listener().reset(parseStringToProperties(""));
        assertFalse(loggerContext.isPackagingDataEnabled());
        assertEquals(8, loggerContext.getMaxCallerDataDepth());
        assertTrue(loggerContext.getFrameworkPackages().isEmpty());
        assertNull(loggerContext.getProperty("dendrobe"));
    }
}
