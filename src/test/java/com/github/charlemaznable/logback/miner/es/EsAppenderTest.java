package com.github.charlemaznable.logback.miner.es;

import com.github.charlemaznable.es.diamond.EsConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.es.diamond.EsClientElf.buildEsClient;
import static com.github.charlemaznable.es.diamond.EsClientElf.closeEsClient;
import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.ES_CONFIG_GROUP_NAME;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.joor.Reflect.on;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class EsAppenderTest {

    private static final String CLASS_NAME = EsAppenderTest.class.getName();

    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName
            .parse("docker.elastic.co/elasticsearch/elasticsearch")
            .withTag(ELASTICSEARCH_VERSION);

    private static final String ELASTICSEARCH_USERNAME = "elastic";
    private static final String ELASTICSEARCH_PASSWORD = "changeme";

    private static ElasticsearchContainer elasticsearch
            = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withPassword(ELASTICSEARCH_PASSWORD);

    private static RestHighLevelClient esClient;

    private static Logger root;
    private static Logger self;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        elasticsearch.start();

        val esConfig = new EsConfig();
        esConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        esConfig.setUsername(ELASTICSEARCH_USERNAME);
        esConfig.setPassword(ELASTICSEARCH_PASSWORD);
        esClient = buildEsClient(esConfig);

        val createIndexRequest = new CreateIndexRequest("logback.miner");
        val createIndexResponse = esClient.indices()
                .create(createIndexRequest, DEFAULT);
        val openIndexRequest = new OpenIndexRequest("logback.miner");
        val openIndexResponse = esClient.indices().open(openIndexRequest, DEFAULT);

        await().forever().until(() -> nonNull(
                DiamondSubscriber.getInstance().getDiamondRemoteChecker()));
        Object diamondRemoteChecker = DiamondSubscriber.getInstance().getDiamondRemoteChecker();
        await().forever().until(() -> 1 <= on(diamondRemoteChecker)
                .field("diamondAllListener").field("allListeners").call("size").<Integer>get());

        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        self = LoggerFactory.getLogger(EsAppenderTest.class);
    }

    @AfterAll
    public static void afterAll() {
        closeEsClient(esClient);
        elasticsearch.stop();
    }

    @Test
    public void testEsAppender() {
        MockDiamondServer.setUpMockServer();

        // 1. 内部配置, 从无到有
        MockDiamondServer.setConfigInfo(ES_CONFIG_GROUP_NAME, "DEFAULT", "" +
                "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n");
        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[appenders]=[es]\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n" +
                CLASS_NAME + "[es.index]=logback.miner\n");
        await().forever().until(future1::isDone);

        if (isNull(EsClientManager.getEsClient("DEFAULT"))) {
            root.info("none es log");
            self.info("none es log");
        }

        await().until(() -> nonNull(EsClientManager.getEsClient("DEFAULT")));
        root.info("root es log 1");
        self.info("self es log 1");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log 1"));

        // 2. 内部配置, EsConfig未更改
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.miner\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n");
        await().forever().until(future2::isDone);

        assertNotNull(EsClientManager.getEsClient("DEFAULT"));

        // 3. 内部配置, EsConfig更改
        MockDiamondServer.setConfigInfo(ES_CONFIG_GROUP_NAME, "DEFAULT", "" +
                "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n" +
                "connectionTimeout=2\n");
        val future3 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n" +
                CLASS_NAME + "[es.index]=logback.miner\n");
        await().forever().until(future3::isDone);

        root.info("root es log 2");
        self.info("self es log 2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log 2"));

        // 4. 内部配置, EsConfig删除
        ConcurrentHashMap<DiamondAxis, String> mocks = onClass(MockDiamondServer.class).field("mocks").get();
        mocks.remove(DiamondAxis.makeAxis(ES_CONFIG_GROUP_NAME, "DEFAULT"));
        val future4 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.miner\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n");
        await().forever().until(future4::isDone);

        await().until(() -> isNull(EsClientManager.getEsClient("DEFAULT")));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testEsAppenderExternal() {
        MockDiamondServer.setUpMockServer();

        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=CUSTOM\n" +
                CLASS_NAME + "[es.index]=logback.miner\n");
        await().forever().until(future1::isDone);

        // 1. 外部导入, 从无到有
        val customConfig = new EsConfig();
        customConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        customConfig.setUsername(ELASTICSEARCH_USERNAME);
        customConfig.setPassword(ELASTICSEARCH_PASSWORD);
        val customClient = buildEsClient(customConfig);
        EsClientManager.putExternalEsClient("CUSTOM", customClient);

        if (isNull(EsClientManager.getEsClient("CUSTOM"))) {
            root.info("none es log");
            self.info("none es log");
        }

        await().until(() -> nonNull(EsClientManager.getEsClient("CUSTOM")));
        root.info("root es log custom");
        self.info("self es log custom");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log custom"));

        // 2. 重新加载, 不影响外部导入
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.miner\n" +
                CLASS_NAME + "[es.name]=CUSTOM\n");
        await().forever().until(future2::isDone);

        await().pollDelay(Duration.ofSeconds(5)).until(() -> true);
        root.info("root es log reload");
        self.info("self es log reload");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log reload"));

        // 3. 清除外部导入, 不影响es实例运行
        EsClientManager.putExternalEsClient("CUSTOM", null);

        await().until(() -> isNull(EsClientManager.getEsClient("CUSTOM")));

        closeEsClient(customClient);

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testEsAppenderCross() {
        MockDiamondServer.setUpMockServer();

        // 1. 内部配置转外部导入
        MockDiamondServer.setConfigInfo(ES_CONFIG_GROUP_NAME, "CROSS", "" +
                "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n");
        val future1 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=CROSS\n" +
                CLASS_NAME + "[es.index]=logback.miner\n");
        await().forever().until(future1::isDone);

        await().until(() -> nonNull(EsClientManager.getEsClient("CROSS")));
        root.info("root es log cross internal1");
        self.info("self es log cross internal1");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross internal1"));

        val crossConfig = new EsConfig();
        crossConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        crossConfig.setUsername(ELASTICSEARCH_USERNAME);
        crossConfig.setPassword(ELASTICSEARCH_PASSWORD);
        val crossClient = buildEsClient(crossConfig);
        EsClientManager.putExternalEsClient("CROSS", crossClient);
        await().until(() -> crossClient == EsClientManager.getEsClient("CROSS"));

        root.info("root es log cross external");
        self.info("self es log cross external");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross external"));

        // 2. 重新加载, 外部导入被内部配置覆盖
        val future2 = MockDiamondServer.updateDiamond("Logback", "test", "" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.miner\n" +
                CLASS_NAME + "[es.name]=CROSS\n");
        await().forever().until(future2::isDone);

        root.info("root es log cross internal2");
        self.info("self es log cross internal2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross internal2"));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testEsAppenderCoverage() {
        assertNull(EsClientManager.getEsClient(null));
        assertDoesNotThrow(() -> EsClientManager.putExternalEsClient(null, null));
    }

    @SneakyThrows
    private void assertSearchContent(String content) {
        val searchRequest = new SearchRequest();
        searchRequest.source(SearchSourceBuilder.searchSource()
                .query(QueryBuilders.matchQuery("event.message", content)));
        val searchResponse = esClient.search(searchRequest, DEFAULT);
        val searchResponseHits = searchResponse.getHits();
        assertTrue(searchResponseHits.getHits().length > 0);
        val responseMap = searchResponseHits.getAt(0).getSourceAsMap();
        //noinspection unchecked
        assertEquals(content, ((Map<String, String>) responseMap.get("event")).get("message"));
    }
}
