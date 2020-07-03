package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class EffectorTest {

    @Test
    public void testEffector() {
        val loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        val effectorContext = new EffectorContext(loggerContext);

        val root = effectorContext.getEffector(Logger.ROOT_LOGGER_NAME);
        val self = effectorContext.getEffector(log.getName());

        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());

        root.setConsoleLevel(Level.INFO);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.INFO_INT, self.getConsoleEffectiveLevelInt());

        self.setConsoleLevel(Level.WARN);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());

        root.setConsoleLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());

        self.setConsoleLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());
    }
}
