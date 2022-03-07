package com.github.charlemaznable.logback.dendrobe.es;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.dendrobe.appender.AsyncAppender;
import com.github.charlemaznable.logback.dendrobe.effect.Effector;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.helpers.Util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.bingoohuang.utils.lang.Mapp.desc;
import static com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf.buildEventMap;
import static com.github.charlemaznable.logback.dendrobe.es.EsCaches.EsLogBeanEsNameCache.getEsName;
import static com.github.charlemaznable.logback.dendrobe.es.EsCaches.EsLogBeanPresentCache.isEsLogBeanPresent;
import static com.github.charlemaznable.logback.dendrobe.es.EsCaches.EsLogIndexCache.getEsIndex;
import static com.github.charlemaznable.logback.dendrobe.es.EsEffectorBuilder.ES_EFFECTOR;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
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

            // 公共参数, 包含event/mdc/ctx-property
            val paramMap = buildEventMap(eventObject);
            val westId = paramMap.westId();

            val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
            val arguments = Arrays.stream(argumentArray)
                    .filter(arg -> nonNull(arg) && isEsLogBeanPresent(arg.getClass()))
                    .collect(Collectors.toList());
            // 日志不包含@EsLogBean注解的参数
            if (arguments.isEmpty()) {
                val esClient = EsClientManager.getEsClient(esName);
                if (isNull(esClient) || isNull(esIndex)) return;
                indexAsync(esClient, esIndex, westId, paramMap);
                return;
            }

            // 遍历@EsLogBean注解的参数
            for (val argument : arguments) {
                val clazz = argument.getClass();
                val esClient = EsClientManager.getEsClient(getEsName(clazz, esName));
                val index = getEsIndex(clazz, esIndex);
                if (isNull(esClient) || isNull(index)) continue;

                val currentMap = newHashMap(paramMap);
                currentMap.put("arg", desc(argument)); // trans to map
                indexAsync(esClient, index, westId, currentMap);
            }
        }

        @SneakyThrows
        private void indexAsync(RestHighLevelClient client, String index, String id, Map<String, ?> source) {
            val indexRequest = new IndexRequest(index).id(id);
            indexRequest.source(source, XContentType.JSON);
            client.indexAsync(indexRequest, DEFAULT, new ActionListener<IndexResponse>() {
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
