package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.core.es.EsConfig;
import com.github.charlemaznable.logback.dendrobe.EsLogBean;
import com.github.charlemaznable.logback.dendrobe.EsLogIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

import static com.github.charlemaznable.core.es.EsClientElf.buildEsClient;
import static com.github.charlemaznable.core.es.EsClientElf.closeEsClient;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static com.github.charlemaznable.logback.dendrobe.es.TestEsConfigService.removeConfig;
import static com.github.charlemaznable.logback.dendrobe.es.TestEsConfigService.setConfig;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
@Slf4j
public class EsAppenderTest implements EsClientManagerListener {

    private static final String CLASS_NAME = EsAppenderTest.class.getName();

    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName
            .parse("docker.elastic.co/elasticsearch/elasticsearch")
            .withTag(ELASTICSEARCH_VERSION);

    static final String ELASTICSEARCH_USERNAME = "elastic";
    static final String ELASTICSEARCH_PASSWORD = "changeme";

    static ElasticsearchContainer elasticsearch
            = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withPassword(ELASTICSEARCH_PASSWORD);

    private static RestHighLevelClient esClient;

    private static Logger root;
    private static Logger self;

    private boolean configured;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        elasticsearch.start();

        val esConfig = new EsConfig();
        esConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        esConfig.setUsername(ELASTICSEARCH_USERNAME);
        esConfig.setPassword(ELASTICSEARCH_PASSWORD);
        esClient = buildEsClient(esConfig);

        val createIndexRequest = new CreateIndexRequest("logback.dendrobe");
        val createIndexResponse = esClient.indices()
                .create(createIndexRequest, DEFAULT);
        val openIndexRequest = new OpenIndexRequest("logback.dendrobe");
        val openIndexResponse = esClient.indices().open(openIndexRequest, DEFAULT);

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
        EsClientManager.addListener(this);

        // 1. 内部配置, 从无到有
        configured = false;
        setConfig("DEFAULT", "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[appenders]=[es]\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root es log {}", new TestLog1("1"));
        self.info("self es log {}", new TestLog1("1"));
        await().forever().untilAsserted(() ->
                assertSearchContent("self es log 1", "1"));

        // 2. 内部配置, EsConfig未更改
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n"));

        assertNotNull(EsClientManager.getEsClient("DEFAULT"));

        // 3. 内部配置, EsConfig更改
        configured = false;
        setConfig("DEFAULT", "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n" +
                "connectionTimeout=2\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root es log {}", new TestLog2("2"));
        self.info("self es log {}", new TestLog2("2"));
        await().forever().untilAsserted(() ->
                assertSearchContent("self es log 2", "2"));

        // 4. 内部配置, EsConfig删除
        removeConfig("DEFAULT");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n" +
                CLASS_NAME + "[es.name]=DEFAULT\n"));
        await().until(() -> isNull(EsClientManager.getEsClient("DEFAULT")));

        EsClientManager.removeListener(this);
    }

    @Test
    public void testEsAppenderExternal() {
        EsClientManager.addListener(this);

        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=CUSTOM\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n"));

        // 1. 外部导入, 从无到有
        val customConfig = new EsConfig();
        customConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        customConfig.setUsername(ELASTICSEARCH_USERNAME);
        customConfig.setPassword(ELASTICSEARCH_PASSWORD);
        val customClient = buildEsClient(customConfig);
        EsClientManager.putExternalEsClient("CUSTOM", customClient);
        await().forever().until(() -> configured);

        root.info("root es log custom");
        self.info("self es log custom");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log custom", null));

        // 2. 重新加载, 不影响外部导入
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n" +
                CLASS_NAME + "[es.name]=CUSTOM\n"));

        root.info("root es log reload");
        self.info("self es log reload");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log reload", null));

        // 3. 清除外部导入, 不影响es实例运行
        EsClientManager.putExternalEsClient("CUSTOM", null);
        await().until(() -> isNull(EsClientManager.getEsClient("CUSTOM")));

        closeEsClient(customClient);

        EsClientManager.removeListener(this);
    }

    @Test
    public void testEsAppenderCross() {
        EsClientManager.addListener(this);

        // 1. 内部配置转外部导入
        configured = false;
        setConfig("CROSS", "uris=" + elasticsearch.getHttpHostAddress() + "\n" +
                "username=" + ELASTICSEARCH_USERNAME + "\n" +
                "password=" + ELASTICSEARCH_PASSWORD + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.name]=CROSS\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root es log cross internal1");
        self.info("self es log cross internal1");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross internal1", null));

        configured = false;
        val crossConfig = new EsConfig();
        crossConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
        crossConfig.setUsername(ELASTICSEARCH_USERNAME);
        crossConfig.setPassword(ELASTICSEARCH_PASSWORD);
        val crossClient = buildEsClient(crossConfig);
        EsClientManager.putExternalEsClient("CROSS", crossClient);
        await().forever().until(() -> configured);

        root.info("root es log cross external");
        self.info("self es log cross external");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross external", null));

        // 2. 重新加载, 外部导入被内部配置覆盖
        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[es.level]=info\n" +
                CLASS_NAME + "[es.index]=logback.dendrobe\n" +
                CLASS_NAME + "[es.name]=CROSS\n"));
        await().forever().until(() -> configured);

        root.info("root es log cross internal2");
        self.info("self es log cross internal2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertSearchContent("self es log cross internal2", null));

        EsClientManager.removeListener(this);
    }

    @Test
    public void testEsAppenderCoverage() {
        assertNull(EsClientManager.getEsClient(null));
        assertDoesNotThrow(() -> EsClientManager.putExternalEsClient(null, null));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void assertSearchContent(String content, String info) {
        val searchRequest = new SearchRequest();
        searchRequest.source(SearchSourceBuilder.searchSource()
                .query(QueryBuilders.matchPhraseQuery("event.message", content)));
        val searchResponse = esClient.search(searchRequest, DEFAULT);
        val searchResponseHits = searchResponse.getHits();
        assertTrue(searchResponseHits.getHits().length > 0);
        val responseMap = searchResponseHits.getAt(0).getSourceAsMap();
        assertEquals(content, ((Map<String, String>) responseMap.get("event")).get("message"));
        if (nonNull(info)) assertEquals(info, ((Map<String, String>) responseMap.get("arg")).get("info"));
    }

    @Override
    public void configuredEsClient(String esName) {
        configured = true;
    }

    @EsLogBean
    @AllArgsConstructor
    @Getter
    public static class TestLog1 {

        private String info;

        @Override
        public String toString() {
            return info;
        }
    }

    @EsLogBean
    @EsLogIndex
    @AllArgsConstructor
    @Getter
    public static class TestLog2 {

        private String info;

        @Override
        public String toString() {
            return info;
        }
    }
}
