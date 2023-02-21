package com.github.charlemaznable.logback.dendrobe.kafka;

import com.github.charlemaznable.core.kafka.KafkaClientElf;
import com.github.charlemaznable.logback.dendrobe.KafkaLogBean;
import com.github.charlemaznable.logback.dendrobe.KafkaLogTopic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.kafka.KafkaClientElf.buildConsumer;
import static com.github.charlemaznable.core.kafka.KafkaClientElf.buildProducer;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.logback.dendrobe.TestHotUpdater.listener;
import static com.github.charlemaznable.logback.dendrobe.kafka.TestKafkaConfigService.removeConfig;
import static com.github.charlemaznable.logback.dendrobe.kafka.TestKafkaConfigService.setConfig;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class KafkaAppenderTest implements KafkaClientManagerListener {

    private static final String CLASS_NAME = KafkaAppenderTest.class.getName();

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:6.2.1");

    static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    private static KafkaProducer<String, String> kafkaProducer;
    private static KafkaConsumer<String, String> kafkaConsumer;

    private static Logger root;
    private static Logger self;

    private boolean configured;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        kafka.start();
        val bootstrapServers = kafka.getBootstrapServers();

        val producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducer = buildProducer(producerConfig);

        val consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaConsumer = buildConsumer(consumerConfig);
        kafkaConsumer.assign(Collections.singleton(new TopicPartition("logback.dendrobe", 0)));

        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        self = LoggerFactory.getLogger(KafkaAppenderTest.class);
    }

    @AfterAll
    public static void afterAll() {
        kafkaProducer.close();
        kafkaConsumer.close();
        kafka.stop();
    }

    @Test
    public void testKafkaAppender() {
        KafkaClientManager.addListener(this);

        // 1. 内部配置, 从无到有
        configured = false;
        setConfig("DEFAULT", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG +
                "=" + kafka.getBootstrapServers() + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[appenders]=[kafka]\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.name]=DEFAULT\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root kafka log {}", new TestLog1("1"));
        self.info("self kafka log {}", new TestLog1("1"));
        await().forever().untilAsserted(() ->
                assertConsumedContent("self kafka log 1", "1"));

        // 2. 内部配置, KafkaConfig未更改
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n" +
                CLASS_NAME + "[kafka.name]=DEFAULT\n"));

        assertNotNull(KafkaClientManager.getKafkaClient("DEFAULT"));

        // 3. 内部配置, KafkaConfig更改
        configured = false;
        setConfig("DEFAULT", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG +
                "=" + kafka.getBootstrapServers() + "\n" +
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG + "=ignored-config\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.name]=DEFAULT\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root kafka log {}", new TestLog2("2"));
        self.info("self kafka log {}", new TestLog2("2"));
        await().forever().untilAsserted(() ->
                assertConsumedContent("self kafka log 2", "2"));

        // 4. 内部配置, KafkaConfig删除
        removeConfig("DEFAULT");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n" +
                CLASS_NAME + "[kafka.name]=DEFAULT\n"));
        await().until(() -> isNull(KafkaClientManager.getKafkaClient("DEFAULT")));

        KafkaClientManager.removeListener(this);
    }

    @Test
    public void testKafkaAppenderExternal() {
        KafkaClientManager.addListener(this);

        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.name]=CUSTOM\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n"));

        // 1. 外部导入, 从无到有
        val producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        val customProducer = KafkaClientElf.<String, String>buildProducer(producerConfig);
        KafkaClientManager.putExternalKafkaClient("CUSTOM", customProducer);
        await().forever().until(() -> configured);

        root.info("root kafka log custom");
        self.info("self kafka log custom");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertConsumedContent("self kafka log custom", null));

        // 2. 重新加载, 不影响外部导入
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n" +
                CLASS_NAME + "[kafka.name]=CUSTOM\n"));

        root.info("root kafka log reload");
        self.info("self kafka log reload");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertConsumedContent("self kafka log reload", null));

        // 3. 清除外部导入, 不影响es实例运行
        KafkaClientManager.putExternalKafkaClient("CUSTOM", null);
        await().until(() -> isNull(KafkaClientManager.getKafkaClient("CUSTOM")));

        customProducer.close();

        KafkaClientManager.removeListener(this);
    }

    @Test
    public void testKafkaAppenderCross() {
        KafkaClientManager.addListener(this);

        // 1. 内部配置转外部导入
        configured = false;
        setConfig("CROSS", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG +
                "=" + kafka.getBootstrapServers() + "\n");
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.name]=CROSS\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n"));
        await().forever().until(() -> configured);

        root.info("root kafka log cross internal1");
        self.info("self kafka log cross internal1");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertConsumedContent("self kafka log cross internal1", null));

        configured = false;
        val producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        val crossProducer = KafkaClientElf.<String, String>buildProducer(producerConfig);
        KafkaClientManager.putExternalKafkaClient("CROSS", crossProducer);
        await().forever().until(() -> configured);

        root.info("root kafka log cross external");
        self.info("self kafka log cross external");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertConsumedContent("self kafka log cross external", null));

        // 2. 重新加载, 外部导入被内部配置覆盖
        configured = false;
        listener().reset(parseStringToProperties("" +
                "root[console.level]=info\n" +
                CLASS_NAME + "[kafka.level]=info\n" +
                CLASS_NAME + "[kafka.topic]=logback.dendrobe\n" +
                CLASS_NAME + "[kafka.name]=CROSS\n"));
        await().forever().until(() -> configured);

        root.info("root kafka log cross internal2");
        self.info("self kafka log cross internal2");
        await().timeout(Duration.ofSeconds(20)).untilAsserted(() ->
                assertConsumedContent("self kafka log cross internal2", null));

        KafkaClientManager.removeListener(this);
    }

    @Test
    public void testKafkaAppenderCoverage() {
        assertNull(KafkaClientManager.getKafkaClient(null));
        assertDoesNotThrow(() -> KafkaClientManager.putExternalKafkaClient(null, null));
    }

    @SuppressWarnings("unchecked")
    private void assertConsumedContent(String content, String info) {
        while (true) {
            val records = kafkaConsumer.poll(Duration.ofSeconds(1));
            for (val record : records) {
                val valueMap = unJson(record.value());
                assertEquals(content, ((Map<String, String>) valueMap.get("event")).get("message"));
                if (nonNull(info)) assertEquals(info, ((Map<String, String>) valueMap.get("arg")).get("info"));
                return;
            }
        }
    }

    @Override
    public void configuredKafkaClient(String kafkaName) {
        configured = true;
    }

    @KafkaLogBean
    @AllArgsConstructor
    @Getter
    public static class TestLog1 {

        private String info;

        @Override
        public String toString() {
            return info;
        }
    }

    @KafkaLogBean
    @KafkaLogTopic
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
