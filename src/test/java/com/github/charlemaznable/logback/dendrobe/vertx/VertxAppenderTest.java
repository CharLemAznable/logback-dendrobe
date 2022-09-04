package com.github.charlemaznable.logback.dendrobe.vertx;

import com.github.charlemaznable.core.vertx.VertxElf;
import com.github.charlemaznable.logback.dendrobe.VertxLogAddress;
import com.github.charlemaznable.logback.dendrobe.VertxLogBean;
import com.hazelcast.config.Config;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static com.github.charlemaznable.logback.dendrobe.vertx.TestVertxOptionsService.removeConfig;
import static com.github.charlemaznable.logback.dendrobe.vertx.TestVertxOptionsService.setConfig;
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
public class VertxAppenderTest implements VertxManagerListener {

    private static final String CLASS_NAME = VertxAppenderTest.class.getName();

    private static Vertx vertx;
    private static String lastEventMessage;
    private static String lastArgInfo;
    private static Logger root;
    private static Logger self;

    private boolean configured;

    @BeforeAll
    public static void beforeAll() {
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(10);
        vertxOptions.setClusterManager(new TestHazelcastClusterManager());
        vertx = VertxElf.buildVertx(vertxOptions);
        vertx.eventBus().consumer("logback.dendrobe",
                (Handler<Message<JsonObject>>) event -> {
                    try {
                        lastEventMessage = event.body().getJsonObject("event").getString("message");
                        val arg = event.body().getJsonObject("arg");
                        if (nonNull(arg)) lastArgInfo = arg.getString("info");
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
        VertxManager.addListener(this);

        // 1. 内部配置, 从无到有
        configured = false;
        setConfig("DEFAULT", "workerPoolSize=42\n" +
                "clusterManager=@" + TestHazelcastClusterManager.class.getName() + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[appenders]=[vertx]\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.name]=DEFAULT\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));
        await().forever().until(() -> configured);

        root.info("root vertx log {}", new TestLog1("old"));
        self.info("self vertx log {}", new TestLog1("old"));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertEquals("self vertx log old", lastEventMessage);
            assertEquals("old", lastArgInfo);
        });

        // 2. 内部配置, VertxConfig未更改
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[vertx.name]=DEFAULT\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));

        assertNotNull(VertxManager.getVertx("DEFAULT"));

        // 3. 内部配置, VertxConfig更改
        configured = false;
        setConfig("DEFAULT", "workerPoolSize=24\n" +
                "clusterManager=@" + TestHazelcastClusterManager.class.getName() + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.name]=DEFAULT\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));
        await().forever().until(() -> configured);

        val vertx = VertxManager.getVertx("DEFAULT");
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(24, defaultWorkerPoolSize);

        root.info("root vertx log {}", new TestLog2("new"));
        self.info("self vertx log {}", new TestLog2("new"));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertEquals("self vertx log new", lastEventMessage);
            assertEquals("new", lastArgInfo);
        });

        // 4. 内部配置, VertxConfig删除
        removeConfig("DEFAULT");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[vertx.name]=DEFAULT\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));
        await().until(() -> isNull(VertxManager.getVertx("DEFAULT")));

        VertxManager.removeListener(this);
    }

    @Test
    public void testVertxAppenderExternal() {
        VertxManager.addListener(this);

        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.name]=CUSTOM\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));

        // 1. 外部导入, 从无到有
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(42);
        vertxOptions.setClusterManager(new TestHazelcastClusterManager());
        val vertx = VertxElf.buildVertx(vertxOptions);
        VertxManager.putExternalVertx("CUSTOM", vertx);
        await().forever().until(() -> configured);

        root.info("root vertx log custom");
        self.info("self vertx log custom");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log custom", lastEventMessage));

        // 2. 重新加载, 不影响外部导入
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[vertx.name]=CUSTOM\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));

        awaitForSeconds(5);
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
        vertx.eventBus().publish("logback.dendrobe", new JsonObject(eventMap));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("external message", lastEventMessage));

        VertxElf.closeVertx(vertx);

        VertxManager.removeListener(this);
    }

    @Test
    public void testVertxAppenderCross() {
        VertxManager.addListener(this);

        // 1. 内部配置转外部导入
        configured = false;
        setConfig("CROSS", "workerPoolSize=42\n" +
                "clusterManager=@" + TestHazelcastClusterManager.class.getName() + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.name]=CROSS\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));
        await().forever().until(() -> configured);

        root.info("root vertx log cross internal");
        self.info("self vertx log cross internal");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross internal", lastEventMessage));

        configured = false;
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(24);
        vertxOptions.setClusterManager(new TestHazelcastClusterManager());
        val vertx = VertxElf.buildVertx(vertxOptions);
        VertxManager.putExternalVertx("CROSS", vertx);
        await().forever().until(() -> configured);

        Vertx vertxTemp = VertxManager.getVertx("CROSS");
        assertNotNull(vertxTemp);
        int defaultWorkerPoolSize = on(vertxTemp).field("defaultWorkerPoolSize").get();
        assertEquals(24, defaultWorkerPoolSize);

        root.info("root vertx log cross external");
        self.info("self vertx log cross external");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross external", lastEventMessage));

        // 2. 重新加载, 外部导入被内部配置覆盖
        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[vertx.level]=info\n" +
                CLASS_NAME + "[vertx.address]=logback.dendrobe\n" +
                CLASS_NAME + "[vertx.name]=CROSS\n" +
                CLASS_NAME + "[console.level]=off\n" +
                CLASS_NAME + "[eql.level]=off\n"));
        await().forever().until(() -> configured);

        vertxTemp = VertxManager.getVertx("CROSS");
        assertNotNull(vertxTemp);
        defaultWorkerPoolSize = on(vertxTemp).field("defaultWorkerPoolSize").get();
        assertEquals(42, defaultWorkerPoolSize);

        root.info("root vertx log cross internal2");
        self.info("self vertx log cross internal2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("self vertx log cross internal2", lastEventMessage));

        // 3. 外部导入不受影响
        Map<String, String> messageMap = newHashMap();
        messageMap.put("message", "external message");
        Map<String, Object> eventMap = newHashMap();
        eventMap.put("event", messageMap);
        vertx.eventBus().publish("logback.dendrobe", new JsonObject(eventMap));
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertEquals("external message", lastEventMessage));

        VertxManager.removeListener(this);
    }

    @Test
    public void testVertxAppenderCoverage() {
        assertNull(VertxManager.getVertx(null));
        assertDoesNotThrow(() -> VertxManager.putExternalVertx(null, null));
    }

    @Override
    public void configuredVertx(String vertxName) {
        configured = true;
    }

    @VertxLogBean
    @AllArgsConstructor
    @Getter
    public static class TestLog1 {

        private String info;

        @Override
        public String toString() {
            return info;
        }
    }

    @VertxLogBean
    @VertxLogAddress
    @AllArgsConstructor
    @Getter
    public static class TestLog2 {

        private String info;

        @Override
        public String toString() {
            return info;
        }
    }

    public static class TestHazelcastClusterManager extends HazelcastClusterManager {

        public TestHazelcastClusterManager() {
            super();
            val hazelcastConfig = new Config();
            hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
            this.setConfig(hazelcastConfig);
        }
    }
}
