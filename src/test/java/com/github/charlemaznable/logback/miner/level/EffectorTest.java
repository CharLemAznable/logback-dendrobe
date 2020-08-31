package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class EffectorTest {

    @Test
    public void testEffector() {
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test", "root[level]=debug\n");
        await().forever().until(future::isDone);

        val loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        val effectorContext = new EffectorContext(loggerContext);

        val root = effectorContext.getEffector(Logger.ROOT_LOGGER_NAME);
        val self = effectorContext.getEffector(log.getName());

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

        val finish = MockDiamondServer.updateDiamond("Logback", "test", "root[level]=info\n");
        await().forever().until(finish::isDone);
        MockDiamondServer.tearDownMockServer();
    }
}
