package com.github.charlemaznable.logback.dendrobe.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.github.charlemaznable.core.es.EsConfig;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutor;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import lombok.val;
import org.slf4j.helpers.Reporter;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.es.EsClientElf.buildElasticsearchAsyncClient;
import static com.github.charlemaznable.core.es.EsClientElf.closeElasticsearchApiClient;
import static java.util.Objects.isNull;

public final class EsBatchClient extends BatchExecutor<BulkOperation> {

    private final ElasticsearchAsyncClient client;

    public static EsBatchClient startClient(EsConfig esConfig, BatchExecutorConfig batchConfig) {
        return startClient(buildElasticsearchAsyncClient(esConfig), batchConfig);
    }

    public static EsBatchClient startClient(ElasticsearchAsyncClient client, BatchExecutorConfig batchConfig) {
        val batchClient = new EsBatchClient(client, batchConfig);
        batchClient.start();
        return batchClient;
    }

    public static EsBatchClient stopClient(EsBatchClient batchClient) {
        if (isNull(batchClient)) return null;
        batchClient.stop();
        return batchClient;
    }

    public static void closeClient(EsBatchClient batchClient) {
        if (isNull(batchClient)) return;
        closeElasticsearchApiClient(batchClient.client);
    }

    public EsBatchClient(ElasticsearchAsyncClient client, BatchExecutorConfig batchConfig) {
        super(batchConfig);
        this.client = client;
    }

    public void addRequest(String index, String id, Map<String, ?> source) {
        add(BulkOperation.of(bulkBuilder ->
                bulkBuilder.index(indexBuilder ->
                        indexBuilder.index(index).id(id).document(source)
                )));
    }

    @Override
    public void batchExecute(List<BulkOperation> requests) {
        if (isNull(client)) return;
        client.bulk(BulkRequest.of(builder -> builder.operations(requests)))
                .whenComplete(((bulkResponse, exception) -> {
                    if (isNull(exception)) return;
                    Reporter.error("ElasticSearch async failed", exception);
                }));
    }
}
