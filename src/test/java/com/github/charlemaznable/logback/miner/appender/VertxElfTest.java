package com.github.charlemaznable.logback.miner.appender;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.cache.ParamsAppliable;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertx;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.closeVertxQuietly;
import static com.github.charlemaznable.logback.miner.appender.VertxManager.VERTX_CONFIG_GROUP_NAME;
import static com.github.charlemaznable.logback.miner.appender.VertxElf.parseStoneToVertxOptions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VertxElfTest {

    @Test
    public void testVertxOptionsElf() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("VertxConfig", "DEFAULT", "" +
                "eventLoopPoolSize=2\n" +
                "maxEventLoopExecuteTime=5\n" +
                "haEnabled=true\n" +
                "haGroup=___DEFAULT___\n" +
                "maxEventLoopExecuteTimeUnit=SECONDS\n" +
                "eventBusOptions.clustered=yes\n" +
                "blockedThreadCheckIntervalUnit=SECOND\n" +
                "clusterManager=@com.github.charlemaznable.logback.miner.appender.VertxElfTest$CustomHazelcastClusterManager()");

        val configStone = new Miner().getStone(
                VERTX_CONFIG_GROUP_NAME, "DEFAULT");
        assertNotNull(configStone);

        val vertxOptions = parseStoneToVertxOptions(configStone);
        assertEquals(2, vertxOptions.getEventLoopPoolSize());
        assertEquals(5, vertxOptions.getMaxEventLoopExecuteTime());
        assertTrue(vertxOptions.isHAEnabled());
        assertEquals("___DEFAULT___", vertxOptions.getHAGroup());
        assertEquals(TimeUnit.SECONDS, vertxOptions.getMaxEventLoopExecuteTimeUnit());
        assertTrue(vertxOptions.getEventBusOptions().isClustered());
        assertNull(vertxOptions.getBlockedThreadCheckIntervalUnit()); // error config SECOND, should be SECONDS
        assertTrue(vertxOptions.getClusterManager() instanceof CustomHazelcastClusterManager);
        val hazelcastClusterManager = (CustomHazelcastClusterManager) vertxOptions.getClusterManager();
        assertNotNull(hazelcastClusterManager.getConfig());

        assertDoesNotThrow(() -> closeVertxQuietly(null));
        assertDoesNotThrow(() -> closeVertx(null));

        MockDiamondServer.tearDownMockServer();
    }

    public static class CustomHazelcastClusterManager extends HazelcastClusterManager implements ParamsAppliable {

        public CustomHazelcastClusterManager() {
            super();
        }

        public CustomHazelcastClusterManager(Config conf) {
            super(conf);
        }

        public CustomHazelcastClusterManager(HazelcastInstance instance) {
            super(instance);
        }

        @Override
        public void applyParams(String[] strings) {
            this.setConfig(ConfigUtil.loadConfig());
        }
    }
}
