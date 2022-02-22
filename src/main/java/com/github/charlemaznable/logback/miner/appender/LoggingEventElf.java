package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.bingoohuang.westid.WestId;
import lombok.val;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public final class LoggingEventElf {

    private static Map<String, Function<ILoggingEvent, String>> eventConverterMap = newHashMap();

    static {
        eventConverterMap.put("date", new Function<ILoggingEvent, String>() {
            private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

            @Override
            public String apply(ILoggingEvent event) {
                return sdf.format(new Date(event.getTimeStamp()));
            }
        });
        eventConverterMap.put("level", event -> event.getLevel().toString());
        eventConverterMap.put("thread", ILoggingEvent::getThreadName);
        eventConverterMap.put("logger", ILoggingEvent::getLoggerName);
        eventConverterMap.put("message", ILoggingEvent::getFormattedMessage);
        eventConverterMap.put("class", event -> {
            val cda = event.getCallerData();
            if (cda != null && cda.length > 0) {
                return cda[0].getClassName();
            } else {
                return CallerData.NA;
            }
        });
        eventConverterMap.put("method", event -> {
            val cda = event.getCallerData();
            if (cda != null && cda.length > 0) {
                return cda[0].getMethodName();
            } else {
                return CallerData.NA;
            }
        });
        eventConverterMap.put("line", event -> {
            val cda = event.getCallerData();
            if (cda != null && cda.length > 0) {
                return Integer.toString(cda[0].getLineNumber());
            } else {
                return CallerData.NA;
            }
        });
        eventConverterMap.put("file", event -> {
            val cda = event.getCallerData();
            if (cda != null && cda.length > 0) {
                return cda[0].getFileName();
            } else {
                return CallerData.NA;
            }
        });
        eventConverterMap.put("exception", event ->
                ThrowableProxyElf.toString(event.getThrowableProxy()));
        eventConverterMap.put("westId", event ->
                Objects.toString(WestId.next()));
    }

    public static EventMap buildEventMap(ILoggingEvent eventObject) {
        Map<String, Object> paramMap = newHashMap();

        Map<String, String> eventMap = newHashMap();
        for (val eventEntry : eventConverterMap.entrySet()) {
            eventMap.put(eventEntry.getKey(),
                    eventEntry.getValue().apply(eventObject));
        }
        paramMap.put("event", eventMap);

        Map<String, String> mdcMap = newHashMap();
        for (val mdcEntry : eventObject.getMDCPropertyMap().entrySet()) {
            mdcMap.put(mdcEntry.getKey(), mdcEntry.getValue());
        }
        paramMap.put("mdc", mdcMap);

        Map<String, String> propMap = newHashMap();
        for (val propEntry : eventObject.getLoggerContextVO().getPropertyMap().entrySet()) {
            val key = propEntry.getKey();
            propMap.put(key, defaultIfNull(propEntry.getValue(), System.getProperty(key)));
        }
        paramMap.put("property", propMap);

        return new EventMap(paramMap);
    }

    private LoggingEventElf() {}

    public static class EventMap extends HashMap<String, Object> {

        private static final long serialVersionUID = -2969581445867351849L;

        public EventMap(Map<String, Object> map) {
            super(map);
        }

        public String westId() {
            //noinspection unchecked
            return ((Map<String, String>) this.get("event")).get("westId");
        }
    }
}
