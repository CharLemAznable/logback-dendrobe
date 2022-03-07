package com.github.charlemaznable.logback.dendrobe.rollingfile;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("Duplicates")
@Slf4j
public class RollingFileAppenderTest {

    private static final String CLASS_NAME = RollingFileAppenderTest.class.getName();

    @SneakyThrows
    @Test
    public void testRollingFileAppender() {
        FileUtils.deleteQuietly(new File("rolling"));
        awaitForSeconds(5);

        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[level]=debug\n" +
                CLASS_NAME + "[appenders]=[rollingfile]\n" +
                CLASS_NAME + "[rollingfile]=rolling/RollingFileAppenderTest.log\n" +
                CLASS_NAME + "[rollingfile.level]=info\n" +
                CLASS_NAME + "[rollingfile.charset]=utf-8\n" +
                CLASS_NAME + "[rollingfile.pattern]=%5level %message%n\n" +
                CLASS_NAME + "[rollingfile.prudent]=false\n" +
                CLASS_NAME + "[rollingfile.append]=false\n" +
                CLASS_NAME + "[rollingfile.bufferSize]=1024\n" +
                CLASS_NAME + "[rollingfile.immediateflush]=true\n" +
                CLASS_NAME + "[rollingfile.fileNamePattern]=rolling/RollingFileAppenderTest.log\n" +
                CLASS_NAME + "[rollingfile.maxFileSize]=1024\n" +
                CLASS_NAME + "[rollingfile.minIndex]=1\n" +
                CLASS_NAME + "[rollingfile.maxIndex]=7\n" +
                CLASS_NAME + "[rollingfile.maxHistory]=0\n" +
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n"));

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging..");
        awaitForSeconds(3);

        assertFalse(FileUtils.deleteQuietly(new File("rolling/RollingFileAppenderTest.log")));

        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[level]=debug\n" +
                CLASS_NAME + "[rollingfile]=rolling/RollingFileAppenderTest.0.ilog\n" +
                CLASS_NAME + "[rollingfile.level]=info\n" +
                CLASS_NAME + "[rollingfile.charset]=utf-8\n" +
                CLASS_NAME + "[rollingfile.pattern]=%5level %message%n\n" +
                CLASS_NAME + "[rollingfile.prudent]=false\n" +
                CLASS_NAME + "[rollingfile.append]=false\n" +
                CLASS_NAME + "[rollingfile.bufferSize]=1024\n" +
                CLASS_NAME + "[rollingfile.immediateflush]=true\n" +
                CLASS_NAME + "[rollingfile.fileNamePattern]=rolling/RollingFileAppenderTest.%i.ilog\n" +
                CLASS_NAME + "[rollingfile.maxFileSize]=1024\n" +
                CLASS_NAME + "[rollingfile.minIndex]=1\n" +
                CLASS_NAME + "[rollingfile.maxIndex]=7\n" +
                CLASS_NAME + "[rollingfile.maxHistory]=0\n" +
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n"));

        val expectBuilder = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            log.trace("trace logging");
            log.debug("debug logging");
            log.info("info logging");
            log.warn("warn logging");
            log.error("error logging");
            expectBuilder.append(" INFO info logging\n" +
                    " WARN warn logging\n" +
                    "ERROR error logging\n");
        }
        awaitForSeconds(3);

        FileUtils.waitFor(new File("rolling"), 5);
        val ilogs = FileUtils.listFiles(new File("rolling"), new String[]{"ilog"}, false)
                .stream().sorted().collect(Collectors.toList());
        assertTrue(ilogs.size() > 1);
        val ilogBuilder = new StringBuilder();
        for (val logFile : ilogs) {
            ilogBuilder.insert(0, FileUtils.readFileToString(logFile, StandardCharsets.UTF_8));
        }
        assertEquals(expectBuilder.toString(), ilogBuilder.toString());

        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[level]=debug\n" +
                CLASS_NAME + "[rollingfile]=\n" +
                CLASS_NAME + "[rollingfile.level]=info\n" +
                CLASS_NAME + "[rollingfile.charset]=utf-8\n" +
                CLASS_NAME + "[rollingfile.pattern]=%5level %message%n\n" +
                CLASS_NAME + "[rollingfile.prudent]=false\n" +
                CLASS_NAME + "[rollingfile.append]=false\n" +
                CLASS_NAME + "[rollingfile.bufferSize]=1024\n" +
                CLASS_NAME + "[rollingfile.immediateflush]=true\n" +
                CLASS_NAME + "[rollingfile.fileNamePattern]=rolling/RollingFileAppenderTest.%d{yyyyMMddHHmmss}.dlog\n" +
                CLASS_NAME + "[rollingfile.maxFileSize]=1024\n" +
                CLASS_NAME + "[rollingfile.minIndex]=1\n" +
                CLASS_NAME + "[rollingfile.maxIndex]=7\n" +
                CLASS_NAME + "[rollingfile.maxHistory]=0\n" +
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n"));

        expectBuilder.setLength(0);
        for (int i = 0; i < 3; i++) {
            log.trace("trace logging");
            log.debug("debug logging");
            log.info("info logging");
            log.warn("warn logging");
            log.error("error logging");
            expectBuilder.append(" INFO info logging\n" +
                    " WARN warn logging\n" +
                    "ERROR error logging\n");
            awaitForSeconds(1);
        }

        FileUtils.waitFor(new File("rolling"), 5);
        val dlogs = FileUtils.listFiles(new File("rolling"), new String[]{"dlog"}, false)
                .stream().sorted().collect(Collectors.toList());
        assertTrue(dlogs.size() > 1);
        val dlogBuilder = new StringBuilder();
        for (val logFile : dlogs) {
            dlogBuilder.append(FileUtils.readFileToString(logFile, StandardCharsets.UTF_8));
        }
        assertEquals(expectBuilder.toString(), dlogBuilder.toString());

        listener().reset(parseStringToProperties("" +
                CLASS_NAME + "[level]=debug\n" +
                CLASS_NAME + "[rollingfile]=\n" +
                CLASS_NAME + "[rollingfile.level]=info\n" +
                CLASS_NAME + "[rollingfile.charset]=utf-8\n" +
                CLASS_NAME + "[rollingfile.pattern]=%5level %message%n\n" +
                CLASS_NAME + "[rollingfile.prudent]=false\n" +
                CLASS_NAME + "[rollingfile.append]=false\n" +
                CLASS_NAME + "[rollingfile.bufferSize]=1024\n" +
                CLASS_NAME + "[rollingfile.immediateflush]=true\n" +
                CLASS_NAME + "[rollingfile.fileNamePattern]=rolling/RollingFileAppenderTest.%d{yyyyMMddHHmmss}.%i.clog\n" +
                CLASS_NAME + "[rollingfile.maxFileSize]=1024\n" +
                CLASS_NAME + "[rollingfile.minIndex]=1\n" +
                CLASS_NAME + "[rollingfile.maxIndex]=7\n" +
                CLASS_NAME + "[rollingfile.maxHistory]=0\n" +
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n"));

        expectBuilder.setLength(0);
        for (int i = 0; i < 50; i++) {
            log.trace("trace logging");
            log.debug("debug logging");
            log.info("info logging");
            log.warn("warn logging");
            log.error("error logging");
            expectBuilder.append(" INFO info logging\n" +
                    " WARN warn logging\n" +
                    "ERROR error logging\n");
            awaitForMillis(50);
        }

        FileUtils.waitFor(new File("rolling"), 5);
        val clogs = FileUtils.listFiles(new File("rolling"), new String[]{"clog"}, false)
                .stream().sorted().collect(Collectors.toList());
        assertTrue(clogs.size() > 1);
        val clogBuilder = new StringBuilder();
        for (val logFile : clogs) {
            clogBuilder.append(FileUtils.readFileToString(logFile, StandardCharsets.UTF_8));
        }
        assertEquals(expectBuilder.toString(), clogBuilder.toString());
    }
}
