package com.github.charlemaznable.logback.miner.appender;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VertxAppenderTest {

    private static Vertx vertx;
    private static String lastEventMessage;
    private static Logger root;
    private static Logger self;

    @BeforeAll
    public static void beforeAll() {
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(10);
        vertxOptions.getEventBusOptions().setClustered(true);
        vertx = VertxElf.buildVertx(vertxOptions);
        vertx.eventBus().consumer("logback.miner",
                (Handler<Message<JsonObject>>) event -> {
                    try {
                        lastEventMessage = event.body().getJsonObject("event").getString("message");
                    } catch (Exception e) {
                        lastEventMessage = null;
                    }
                });

        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        self = LoggerFactory.getLogger(VertxAppenderTest.class);
    }

    @AfterAll
    public static void afterAll() {
        VertxElf.closeVertx(vertx);
    }

    @Test
    public void testVertxAppender() {
        MockDiamondServer.setUpMockServer();

        // 1. default
        MockDiamondServer.setConfigInfo("VertxConfig", "DEFAULT", "" +
                "workerPoolSize=42\n" +
                "eventBusOptions.clustered=on\n");
        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=DEFAULT\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future1::isDone);

        if (isNull(VertxManager.getVertx("DEFAULT"))) {
            root.info("none vertx log");
            self.info("none vertx log");
        }

        await().until(() -> nonNull(VertxManager.getVertx("DEFAULT")));
        root.info("root vertx log");
        self.info("self vertx log");
        await().untilAsserted(() -> assertEquals("self vertx log", lastEventMessage));

        // 2. reload, vertx config not change
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=DEFAULT\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future2::isDone);

        assertNotNull(VertxManager.getVertx("DEFAULT"));

        // 3. reload, vertx config changed: close previous and new instance
        MockDiamondServer.setConfigInfo("VertxConfig", "DEFAULT", "" +
                "workerPoolSize=24\n" +
                "eventBusOptions.clustered=on\n");
        val future3 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=DEFAULT\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future3::isDone);

        await().timeout(Duration.ofSeconds(20)).until(() -> {
            val vertx = VertxManager.getVertx("DEFAULT");
            if (isNull(vertx)) return false;

            int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
            return 24 == defaultWorkerPoolSize;
        });
        root.info("root vertx log new");
        self.info("self vertx log new");
        await().untilAsserted(() -> assertEquals("self vertx log new", lastEventMessage));

        // 4. put from external, close previous
        VertxManager.putExternalVertx("DEFAULT", vertx);

        await().timeout(Duration.ofSeconds(20)).until(() -> {
            val vertx = VertxManager.getVertx("DEFAULT");
            if (isNull(vertx)) return false;

            int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
            return 10 == defaultWorkerPoolSize;
        });
        root.info("root vertx log ex");
        self.info("self vertx log ex");
        await().untilAsserted(() -> assertEquals("self vertx log ex", lastEventMessage));

        MockDiamondServer.tearDownMockServer();
    }
}
