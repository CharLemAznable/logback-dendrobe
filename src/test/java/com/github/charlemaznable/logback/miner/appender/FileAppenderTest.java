package com.github.charlemaznable.logback.miner.appender;

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

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class FileAppenderTest {

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
    }

    @SneakyThrows
    @Test
    public void testFileAppender() {
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.property[miner]=test\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file]=FileAppenderTest.log\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.charset]=utf-8\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.pattern]=%property{miner} %5level %message%n\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.prudent]=false\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.append]=false\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.bufferSize]=1024\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.immediateflush]=true\n");
        await().forever().until(future::isDone);

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging");
        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.property[miner]=test\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file]=FileAppenderTest.log\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.level]=warn\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.charset]=utf\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.pattern]=%property{miner} %5level %message%n\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.prudent]=false\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.append]=true\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.bufferSize]=1024\n" +
                "com.github.charlemaznable.logback.miner.appender.FileAppenderTest[file.immediateflush]=true\n");
        await().forever().until(future2::isDone);

        log.trace("trace logging append");
        log.debug("debug logging append");
        log.info("info logging append");
        log.warn("warn logging append");
        log.error("error logging append");
        await().pollDelay(Duration.ofSeconds(3)).until(() -> true);

        val output = FileUtils.readFileToString(
                new File("FileAppenderTest.log"), StandardCharsets.UTF_8);
        assertEquals("test  INFO info logging\n" +
                "test  WARN warn logging\n" +
                "test ERROR error logging\n" +
                "test  WARN warn logging append\n" +
                "test ERROR error logging append\n", output);

        MockDiamondServer.tearDownMockServer();
    }
}
