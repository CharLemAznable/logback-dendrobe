package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ch.qos.logback.classic.Level.toLevel;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchPropertyKey;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.getLogger;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class BasicConfigurator extends AppenderConfigurator {

    private static final String CONTEXT_PROPERTY_PREFIX = "context.property[";
    private static final String CONTEXT_PROPERTY_SUFFIX = "]";
    private static final String ADDITIVITY_SUFFIX = "[additivity]";
    private static final String LEVEL_SUFFIX = "[level]";

    private ConcurrentMap<String, Boolean> loggerAdditiveMap = new ConcurrentHashMap<>();

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (startsWithIgnoreCase(key, CONTEXT_PROPERTY_PREFIX) &&
                endsWithIgnoreCase(key, CONTEXT_PROPERTY_SUFFIX)) {
            val propertyKey = fetchPropertyKey(key,
                    CONTEXT_PROPERTY_PREFIX, CONTEXT_PROPERTY_SUFFIX);
            loggerContext.putProperty(propertyKey, value);

        } else if (endsWithIgnoreCase(key, ADDITIVITY_SUFFIX)) {
            val name = fetchLoggerName(key, ADDITIVITY_SUFFIX);
            loggerAdditiveMap.put(name, toBool(value));

        } else if (endsWithIgnoreCase(key, LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, LEVEL_SUFFIX);
            getLogger(loggerContext, name).setLevel(toLevel(value));
        }
    }

    @Override
    public void postConfigurate(LoggerContext loggerContext) {
        super.postConfigurate(loggerContext);

        for (val entry : loggerAdditiveMap.entrySet()) {
            getLogger(loggerContext, entry.getKey())
                    .setAdditive(entry.getValue());
        }
        loggerAdditiveMap.clear();
    }
}
