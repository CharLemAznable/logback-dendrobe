package com.github.charlemaznable.logback.miner.es;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.appender.AsyncAppender;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.Setter;
import lombok.val;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.helpers.Util;

import static com.github.charlemaznable.logback.miner.appender.LoggingEventElf.buildEventMap;
import static com.github.charlemaznable.logback.miner.es.EsEffectorBuilder.ES_EFFECTOR;
import static java.util.Objects.isNull;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

public final class EsAppender extends AsyncAppender {

    public static final String DEFAULT_ES_NAME = "DEFAULT";

    private InternalAppender appender;

    public EsAppender() {
        this.appender = new InternalAppender();
        this.appender.setEsName(DEFAULT_ES_NAME);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public EsAppender setEsName(String esName) {
        this.appender.setEsName(esName);
        return this;
    }

    public EsAppender setEsIndex(String esIndex) {
        this.appender.setEsIndex(esIndex);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured EsAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getEffectorLevelInt(ES_EFFECTOR) > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Setter
        private String esName;
        @Setter
        private String esIndex;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            val esClient = EsClientManager.getEsClient(esName);
            if (isNull(esClient) || isNull(esIndex)) return;

            val eventMap = buildEventMap(eventObject);
            val westId = eventMap.westId();
            val indexRequest = new IndexRequest(esIndex).id(westId);
            indexRequest.source(eventMap, XContentType.JSON);
            esClient.indexAsync(indexRequest, DEFAULT, new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    // empty method
                }

                @Override
                public void onFailure(Exception e) {
                    Util.report("ElasticSearch index failed", e);
                }
            });
        }
    }
}
