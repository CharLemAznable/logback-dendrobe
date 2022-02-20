package com.github.charlemaznable.logback.miner.rollingfile;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("Duplicates")
@Slf4j
public class RollingFileAppenderTest {

    private static final String CLASS_NAME = RollingFileAppenderTest.class.getName();

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        Object diamondRemoteChecker = DiamondSubscriber.getInstance().getDiamondRemoteChecker();
        await().forever().until(() -> 1 <= on(diamondRemoteChecker)
                .field("diamondAllListener").field("allListeners").call("size").<Integer>get());
    }

    @SneakyThrows
    @Test
    public void testRollingFileAppender() {
        FileUtils.deleteQuietly(new File("rolling"));
        await().pollDelay(Duration.ofSeconds(5)).until(() -> true);

        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
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
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n");
        await().forever().until(future::isDone);

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging..");
        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        assertFalse(FileUtils.deleteQuietly(new File("rolling/RollingFileAppenderTest.log")));

        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
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
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n");
        await().forever().until(future2::isDone);

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
        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        FileUtils.waitFor(new File("rolling"), 5);
        val ilogs = FileUtils.listFiles(new File("rolling"), new String[]{"ilog"}, false)
                .stream().sorted().collect(Collectors.toList());
        assertTrue(ilogs.size() > 1);
        val ilogBuilder = new StringBuilder();
        for (val logFile : ilogs) {
            ilogBuilder.insert(0, FileUtils.readFileToString(logFile, StandardCharsets.UTF_8));
        }
        assertEquals(expectBuilder.toString(), ilogBuilder.toString());

        val future3 = MockDiamondServer.updateDiamond("Logback", "test", "" +
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
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n");
        await().forever().until(future3::isDone);

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
            await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
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

        val future4 = MockDiamondServer.updateDiamond("Logback", "test", "" +
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
                CLASS_NAME + "[rollingfile.cleanHistoryOnStart]=N\n");
        await().forever().until(future4::isDone);

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
            await().pollDelay(Duration.ofMillis(50)).until(() -> true);
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

        MockDiamondServer.tearDownMockServer();
    }
}
