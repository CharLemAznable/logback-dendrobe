package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class EffectorTest {

    @Test
    public void testEffector() {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var effectorContext = new EffectorContext(loggerContext);

        var root = effectorContext.getEffector(Logger.ROOT_LOGGER_NAME);
        var self = effectorContext.getEffector(log.getName());

        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(root.getDqlLevel());
        assertEquals(Level.DEBUG_INT, root.getDqlEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.DEBUG_INT, self.getDqlEffectiveLevelInt());

        root.setConsoleLevel(Level.INFO);
        root.setDqlLevel(Level.WARN);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertEquals(Level.WARN, root.getDqlLevel());
        assertEquals(Level.WARN_INT, root.getDqlEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.INFO_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.WARN_INT, self.getDqlEffectiveLevelInt());

        self.setConsoleLevel(Level.WARN);
        self.setDqlLevel(Level.INFO);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertEquals(Level.WARN, root.getDqlLevel());
        assertEquals(Level.WARN_INT, root.getDqlEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());
        assertEquals(Level.INFO, self.getDqlLevel());
        assertEquals(Level.INFO_INT, self.getDqlEffectiveLevelInt());

        root.setConsoleLevel(null);
        root.setDqlLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(root.getDqlLevel());
        assertEquals(Level.DEBUG_INT, root.getDqlEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());
        assertEquals(Level.INFO, self.getDqlLevel());
        assertEquals(Level.INFO_INT, self.getDqlEffectiveLevelInt());

        self.setConsoleLevel(null);
        self.setDqlLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(root.getDqlLevel());
        assertEquals(Level.DEBUG_INT, root.getDqlEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.DEBUG_INT, self.getDqlEffectiveLevelInt());
    }
}
