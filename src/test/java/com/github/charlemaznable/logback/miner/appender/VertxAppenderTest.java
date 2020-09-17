package com.github.charlemaznable.logback.miner.appender;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.vertx.diamond.VertxDiamondElf.VERTX_OPTIONS_GROUP_NAME;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class VertxAppenderTest {

    private static Vertx vertx;
    private static String lastEventMessage;
    private static Logger root;
    private static Logger self;

    @BeforeAll
    public static void beforeAll() {
        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
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

        // 1. 内部配置, 从无到有
        MockDiamondServer.setConfigInfo(VERTX_OPTIONS_GROUP_NAME, "DEFAULT", "" +
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
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log", lastEventMessage));

        // 2. 内部配置, VertxConfig未更改
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

        // 3. 内部配置, VertxConfig更改
        MockDiamondServer.setConfigInfo(VERTX_OPTIONS_GROUP_NAME, "DEFAULT", "" +
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
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log new", lastEventMessage));

        // 4. 内部配置, VertxConfig删除
        ConcurrentHashMap<DiamondAxis, String> mocks = on(MockDiamondServer.class).field("mocks").get();
        mocks.remove(DiamondAxis.makeAxis(VERTX_OPTIONS_GROUP_NAME, "DEFAULT"));
        val future4 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=DEFAULT\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future4::isDone);

        await().until(() -> isNull(VertxManager.getVertx("DEFAULT")));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testVertxAppenderExternal() {
        MockDiamondServer.setUpMockServer();

        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=CUSTOM\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future1::isDone);

        // 1. 外部导入, 从无到有
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(42);
        vertxOptions.getEventBusOptions().setClustered(true);
        val vertx = VertxElf.buildVertx(vertxOptions);
        VertxManager.putExternalVertx("CUSTOM", vertx);

        if (isNull(VertxManager.getVertx("CUSTOM"))) {
            root.info("none vertx log");
            self.info("none vertx log");
        }

        await().until(() -> nonNull(VertxManager.getVertx("CUSTOM")));
        root.info("root vertx log custom");
        self.info("self vertx log custom");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log custom", lastEventMessage));

        // 2. 重新加载, 不影响外部导入
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=CUSTOM\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future2::isDone);

        await().pollDelay(Duration.ofSeconds(5)).until(() -> true);
        root.info("root vertx log reload");
        self.info("self vertx log reload");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log reload", lastEventMessage));

        // 3. 清除外部导入, 不影响vertx实例运行
        VertxManager.putExternalVertx("CUSTOM", null);

        await().until(() -> isNull(VertxManager.getVertx("CUSTOM")));
        Map<String, String> messageMap = newHashMap();
        messageMap.put("message", "external message");
        Map<String, Object> eventMap = newHashMap();
        eventMap.put("event", messageMap);
        vertx.eventBus().publish("logback.miner", new JsonObject(eventMap));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("external message", lastEventMessage));

        VertxElf.closeVertx(vertx);

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testVertxAppenderCross() {
        MockDiamondServer.setUpMockServer();

        // 1. 内部配置转外部导入
        MockDiamondServer.setConfigInfo(VERTX_OPTIONS_GROUP_NAME, "CROSS", "" +
                "workerPoolSize=42\n" +
                "eventBusOptions.clustered=on\n");
        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=CROSS\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future1::isDone);

        await().until(() -> nonNull(VertxManager.getVertx("CROSS")));
        root.info("root vertx log cross internal");
        self.info("self vertx log cross internal");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross internal", lastEventMessage));

        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(24);
        vertxOptions.getEventBusOptions().setClustered(true);
        val vertx = VertxElf.buildVertx(vertxOptions);
        VertxManager.putExternalVertx("CROSS", vertx);

        await().timeout(Duration.ofSeconds(20)).until(() -> {
            val vertxTemp = VertxManager.getVertx("CROSS");
            if (isNull(vertxTemp)) return false;

            int defaultWorkerPoolSize = on(vertxTemp).field("defaultWorkerPoolSize").get();
            return 24 == defaultWorkerPoolSize;
        });
        root.info("root vertx log cross external");
        self.info("self vertx log cross external");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross external", lastEventMessage));

        // 2. 重新加载, 外部导入被内部配置覆盖
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[additivity]=no\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.level]=info\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.address]=logback.miner\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[vertx.name]=CROSS\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[console.level]=off\n" +
                "com.github.charlemaznable.logback.miner.appender.VertxAppenderTest[dql.level]=off\n");
        await().forever().until(future2::isDone);

        await().timeout(Duration.ofSeconds(20)).until(() -> {
            val vertxTemp = VertxManager.getVertx("CROSS");
            if (isNull(vertxTemp)) return false;

            int defaultWorkerPoolSize = on(vertxTemp).field("defaultWorkerPoolSize").get();
            return 42 == defaultWorkerPoolSize;
        });
        root.info("root vertx log cross internal2");
        self.info("self vertx log cross internal2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross internal2", lastEventMessage));

        // 3. 外部导入不受影响
        Map<String, String> messageMap = newHashMap();
        messageMap.put("message", "external message");
        Map<String, Object> eventMap = newHashMap();
        eventMap.put("event", messageMap);
        vertx.eventBus().publish("logback.miner", new JsonObject(eventMap));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("external message", lastEventMessage));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testVertxAppenderCoverage() {
        assertNull(VertxManager.getVertx(null));
        assertDoesNotThrow(() -> VertxManager.putExternalVertx(null, null));
    }
}
