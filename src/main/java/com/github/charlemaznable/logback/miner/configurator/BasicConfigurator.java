package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchPropertyKey;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class BasicConfigurator implements Configurator {

    private static final String CONTEXT_PROPERTY_PREFIX = "context.property[";
    private static final String CONTEXT_PROPERTY_SUFFIX = "]";
    private static final String ADDITIVITY_SUFFIX = "[additivity]";
    private static final String LEVEL_SUFFIX = "[level]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (startsWithIgnoreCase(key, CONTEXT_PROPERTY_PREFIX) &&
                endsWithIgnoreCase(key, CONTEXT_PROPERTY_SUFFIX)) {
            val propertyKey = fetchPropertyKey(key,
                    CONTEXT_PROPERTY_PREFIX, CONTEXT_PROPERTY_SUFFIX);
            loggerContext.putProperty(propertyKey, value);

        } else if (endsWithIgnoreCase(key, ADDITIVITY_SUFFIX)) {
            val name = fetchLoggerName(key, ADDITIVITY_SUFFIX);
            loggerContext.getLogger(name).setAdditive(toBool(value));

        } else if (endsWithIgnoreCase(key, LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, LEVEL_SUFFIX);
            loggerContext.getLogger(name).setLevel(Level.toLevel(value));

        }
    }
}
