package com.github.charlemaznable.logback.dendrobe.es;

import com.github.charlemaznable.core.es.EsConfig;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutor;
import com.github.charlemaznable.core.lang.concurrent.BatchExecutorConfig;
import lombok.val;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.helpers.Util;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.es.EsClientElf.buildEsClient;
import static com.github.charlemaznable.core.es.EsClientElf.closeEsClient;
import static java.util.Objects.isNull;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

public final class EsBatchClient extends BatchExecutor<DocWriteRequest<?>> {

    private RestHighLevelClient client;

    public static EsBatchClient startClient(EsConfig esConfig, BatchExecutorConfig batchConfig) {
        return startClient(buildEsClient(esConfig), batchConfig);
    }

    public static EsBatchClient startClient(RestHighLevelClient client, BatchExecutorConfig batchConfig) {
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
        closeEsClient(batchClient.client);
    }

    public EsBatchClient(RestHighLevelClient client, BatchExecutorConfig batchConfig) {
        super(batchConfig);
        this.client = client;
    }

    public void addRequest(String index, String id, Map<String, ?> source) {
        add(new IndexRequest(index).id(id).source(source, XContentType.JSON));
    }

    @Override
    public void batchExecute(List<DocWriteRequest<?>> requests) {
        if (requests.isEmpty() || isNull(client)) return;
        client.bulkAsync(new BulkRequest().add(requests), DEFAULT,
                new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse indexResponse) {
                        // empty method
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Util.report("ElasticSearch async failed", e);
                    }
                });
    }
}
