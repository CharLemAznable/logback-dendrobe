package com.github.charlemaznable.logback.dendrobe.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.dendrobe.effect.EffectorContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static com.github.charlemaznable.logback.dendrobe.console.ConsoleEffectorBuilder.CONSOLE_EFFECTOR;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlEffectorBuilder.EQL_EFFECTOR;
import static com.github.charlemaznable.logback.dendrobe.file.FileEffectorBuilder.FILE_EFFECTOR;
import static com.github.charlemaznable.logback.dendrobe.rollingfile.RollingFileEffectorBuilder.ROLLING_FILE_EFFECTOR;
import static com.github.charlemaznable.logback.dendrobe.vertx.VertxEffectorBuilder.VERTX_EFFECTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class EffectorTest {

    @Test
    public void testEffector() {
        listener().reset(parseStringToProperties("root[level]=debug\n"));

        val loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        val effectorContext = new EffectorContext(loggerContext);

        val root = effectorContext.getEffector(Logger.ROOT_LOGGER_NAME);
        val self = effectorContext.getEffector(log.getName());

        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(root.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(root.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(root.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(root.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));
        assertNull(self.getLoggerLevel());
        assertNull(self.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(self.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(self.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(self.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(self.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));

        root.setEffectorLevel(CONSOLE_EFFECTOR, Level.INFO);
        root.setEffectorLevel(EQL_EFFECTOR, Level.WARN);
        root.setEffectorLevel(VERTX_EFFECTOR, Level.ERROR);
        root.setEffectorLevel(FILE_EFFECTOR, Level.INFO);
        root.setEffectorLevel(ROLLING_FILE_EFFECTOR, Level.WARN);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.INFO_INT, root.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertEquals(Level.WARN, root.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.WARN_INT, root.getEffectorLevelInt(EQL_EFFECTOR));
        assertEquals(Level.ERROR, root.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.ERROR_INT, root.getEffectorLevelInt(VERTX_EFFECTOR));
        assertEquals(Level.INFO, root.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.INFO_INT, root.getEffectorLevelInt(FILE_EFFECTOR));
        assertEquals(Level.WARN, root.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.WARN_INT, root.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));
        assertNull(self.getLoggerLevel());
        assertNull(self.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(self.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(self.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.ERROR_INT, self.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(self.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(self.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));

        self.setEffectorLevel(CONSOLE_EFFECTOR, Level.WARN);
        self.setEffectorLevel(EQL_EFFECTOR, Level.INFO);
        self.setEffectorLevel(VERTX_EFFECTOR, Level.TRACE);
        self.setEffectorLevel(FILE_EFFECTOR, Level.WARN);
        self.setEffectorLevel(ROLLING_FILE_EFFECTOR, Level.INFO);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertEquals(Level.INFO, root.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.INFO_INT, root.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertEquals(Level.WARN, root.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.WARN_INT, root.getEffectorLevelInt(EQL_EFFECTOR));
        assertEquals(Level.ERROR, root.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.ERROR_INT, root.getEffectorLevelInt(VERTX_EFFECTOR));
        assertEquals(Level.INFO, root.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.INFO_INT, root.getEffectorLevelInt(FILE_EFFECTOR));
        assertEquals(Level.WARN, root.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.WARN_INT, root.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertEquals(Level.INFO, self.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(EQL_EFFECTOR));
        assertEquals(Level.TRACE, self.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.TRACE_INT, self.getEffectorLevelInt(VERTX_EFFECTOR));
        assertEquals(Level.WARN, self.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(FILE_EFFECTOR));
        assertEquals(Level.INFO, self.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));

        root.setEffectorLevel(CONSOLE_EFFECTOR, null);
        root.setEffectorLevel(EQL_EFFECTOR, null);
        root.setEffectorLevel(VERTX_EFFECTOR, null);
        root.setEffectorLevel(FILE_EFFECTOR, null);
        root.setEffectorLevel(ROLLING_FILE_EFFECTOR, null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(root.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(root.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(root.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(root.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));
        assertNull(self.getLoggerLevel());
        assertEquals(Level.WARN, self.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertEquals(Level.INFO, self.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(EQL_EFFECTOR));
        assertEquals(Level.TRACE, self.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.TRACE_INT, self.getEffectorLevelInt(VERTX_EFFECTOR));
        assertEquals(Level.WARN, self.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.WARN_INT, self.getEffectorLevelInt(FILE_EFFECTOR));
        assertEquals(Level.INFO, self.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.INFO_INT, self.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));

        self.setEffectorLevel(CONSOLE_EFFECTOR, null);
        self.setEffectorLevel(EQL_EFFECTOR, null);
        self.setEffectorLevel(VERTX_EFFECTOR, null);
        self.setEffectorLevel(FILE_EFFECTOR, null);
        self.setEffectorLevel(ROLLING_FILE_EFFECTOR, null);
        assertEquals(Level.DEBUG, root.getLoggerLevel());
        assertNull(root.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(root.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(root.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(root.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(root.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, root.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));
        assertNull(self.getLoggerLevel());
        assertNull(self.getEffectorLevel(CONSOLE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(CONSOLE_EFFECTOR));
        assertNull(self.getEffectorLevel(EQL_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(EQL_EFFECTOR));
        assertNull(self.getEffectorLevel(VERTX_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(VERTX_EFFECTOR));
        assertNull(self.getEffectorLevel(FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(FILE_EFFECTOR));
        assertNull(self.getEffectorLevel(ROLLING_FILE_EFFECTOR));
        assertEquals(Level.DEBUG_INT, self.getEffectorLevelInt(ROLLING_FILE_EFFECTOR));

        log.info("AnswerToTheUltimateQuestionOfLifeTheUniverseAndEverything");
        log.info("42");

        listener().reset(parseStringToProperties("root[level]=info\n"));
    }
}
