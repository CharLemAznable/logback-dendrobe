package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class EffectorTest {

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
    }

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
        assertNull(root.getVertxLevel());
        assertEquals(Level.DEBUG_INT, root.getVertxEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.DEBUG_INT, self.getDqlEffectiveLevelInt());
        assertNull(self.getVertxLevel());
        assertEquals(Level.DEBUG_INT, self.getVertxEffectiveLevelInt());

        root.setConsoleLevel(Level.INFO);
        root.setDqlLevel(Level.WARN);
        root.setVertxLevel(Level.ERROR);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertEquals(Level.WARN, root.getDqlLevel());
        assertEquals(Level.WARN_INT, root.getDqlEffectiveLevelInt());
        assertEquals(Level.ERROR, root.getVertxLevel());
        assertEquals(Level.ERROR_INT, root.getVertxEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.INFO_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.WARN_INT, self.getDqlEffectiveLevelInt());
        assertNull(self.getVertxLevel());
        assertEquals(Level.ERROR_INT, self.getVertxEffectiveLevelInt());

        self.setConsoleLevel(Level.WARN);
        self.setDqlLevel(Level.INFO);
        self.setVertxLevel(Level.TRACE);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getConsoleLevel());
        assertEquals(Level.INFO_INT, root.getConsoleEffectiveLevelInt());
        assertEquals(Level.WARN, root.getDqlLevel());
        assertEquals(Level.WARN_INT, root.getDqlEffectiveLevelInt());
        assertEquals(Level.ERROR, root.getVertxLevel());
        assertEquals(Level.ERROR_INT, root.getVertxEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());
        assertEquals(Level.INFO, self.getDqlLevel());
        assertEquals(Level.INFO_INT, self.getDqlEffectiveLevelInt());
        assertEquals(Level.TRACE, self.getVertxLevel());
        assertEquals(Level.TRACE_INT, self.getVertxEffectiveLevelInt());

        root.setConsoleLevel(null);
        root.setDqlLevel(null);
        root.setVertxLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(root.getDqlLevel());
        assertEquals(Level.DEBUG_INT, root.getDqlEffectiveLevelInt());
        assertNull(root.getVertxLevel());
        assertEquals(Level.DEBUG_INT, root.getVertxEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getConsoleLevel());
        assertEquals(Level.WARN_INT, self.getConsoleEffectiveLevelInt());
        assertEquals(Level.INFO, self.getDqlLevel());
        assertEquals(Level.INFO_INT, self.getDqlEffectiveLevelInt());
        assertEquals(Level.TRACE, self.getVertxLevel());
        assertEquals(Level.TRACE_INT, self.getVertxEffectiveLevelInt());

        self.setConsoleLevel(null);
        self.setDqlLevel(null);
        self.setVertxLevel(null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, root.getConsoleEffectiveLevelInt());
        assertNull(root.getDqlLevel());
        assertEquals(Level.DEBUG_INT, root.getDqlEffectiveLevelInt());
        assertNull(root.getVertxLevel());
        assertEquals(Level.DEBUG_INT, root.getVertxEffectiveLevelInt());
        assertNull(self.getLoggerLevel());
        assertNull(self.getConsoleLevel());
        assertEquals(Level.DEBUG_INT, self.getConsoleEffectiveLevelInt());
        assertNull(self.getDqlLevel());
        assertEquals(Level.DEBUG_INT, self.getDqlEffectiveLevelInt());
        assertNull(self.getVertxLevel());
        assertEquals(Level.DEBUG_INT, self.getVertxEffectiveLevelInt());

        val finish = MockDiamondServer.updateDiamond("Logback", "test", "root[level]=info\n");
        await().forever().until(finish::isDone);
        MockDiamondServer.tearDownMockServer();
    }
}
