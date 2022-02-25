package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.bingoohuang.westid.WestId;
import lombok.NoArgsConstructor;
import lombok.val;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@NoArgsConstructor(access = PRIVATE)
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
        val eventMap = eventConverterMap.entrySet().stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().apply(eventObject)));
        val mdcMap = eventObject.getMDCPropertyMap().entrySet().stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
        val propMap = eventObject.getLoggerContextVO().getPropertyMap().entrySet().stream()
                .collect(toMap(Entry::getKey, e -> defaultIfNull(e.getValue(), System.getProperty(e.getKey()))));
        return new EventMap(of("event", eventMap, "mdc", mdcMap, "property", propMap));
    }

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
