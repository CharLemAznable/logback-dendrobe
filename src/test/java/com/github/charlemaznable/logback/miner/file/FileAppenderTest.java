package com.github.charlemaznable.logback.miner.file;

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

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class FileAppenderTest {

    private static final String CLASS_NAME = FileAppenderTest.class.getName();

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
    public void testFileAppender() {
        MockDiamondServer.setUpMockServer();
        val future = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.property[miner]=test\n" +
                CLASS_NAME + "[appenders]=[file]\n" +
                CLASS_NAME + "[file]=FileAppenderTest.log\n" +
                CLASS_NAME + "[file.level]=info\n" +
                CLASS_NAME + "[file.charset]=utf-8\n" +
                CLASS_NAME + "[file.pattern]=%property{miner} %5level %message%n\n" +
                CLASS_NAME + "[file.prudent]=false\n" +
                CLASS_NAME + "[file.append]=false\n" +
                CLASS_NAME + "[file.bufferSize]=1024\n" +
                CLASS_NAME + "[file.immediateflush]=true\n");
        await().forever().until(future::isDone);

        log.trace("trace logging");
        log.debug("debug logging");
        log.info("info logging");
        log.warn("warn logging");
        log.error("error logging");
        awaitForSeconds(3);

        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "context.property[miner]=test\n" +
                CLASS_NAME + "[file]=FileAppenderTest.log\n" +
                CLASS_NAME + "[file.level]=warn\n" +
                CLASS_NAME + "[file.charset]=utf\n" +
                CLASS_NAME + "[file.pattern]=%property{miner} %5level %message%n\n" +
                CLASS_NAME + "[file.prudent]=false\n" +
                CLASS_NAME + "[file.append]=true\n" +
                CLASS_NAME + "[file.bufferSize]=1024\n" +
                CLASS_NAME + "[file.immediateflush]=true\n");
        await().forever().until(future2::isDone);

        log.trace("trace logging append");
        log.debug("debug logging append");
        log.info("info logging append");
        log.warn("warn logging append");
        log.error("error logging append");
        awaitForSeconds(3);

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
