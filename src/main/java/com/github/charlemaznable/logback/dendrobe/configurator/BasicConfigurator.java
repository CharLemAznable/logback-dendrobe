package com.github.charlemaznable.logback.dendrobe.configurator;

import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;

import static ch.qos.logback.classic.Level.toLevel;
import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.logger;
import static com.github.charlemaznable.logback.dendrobe.configurator.ConfiguratorElf.propertyKey;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

@AutoService(Configurator.class)
public final class BasicConfigurator extends AppenderConfigurator {

    private static final String CONTEXT_PROPERTY_PREFIX = "context.property[";
    private static final String CONTEXT_PROPERTY_SUFFIX = "]";
    private static final String ADDITIVITY_SUFFIX = "[additivity]";
    private static final String LEVEL_SUFFIX = "[level]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (startsWithIgnoreCase(key, CONTEXT_PROPERTY_PREFIX) &&
                endsWithIgnoreCase(key, CONTEXT_PROPERTY_SUFFIX)) {
            loggerContext.putProperty(propertyKey(key,
                    CONTEXT_PROPERTY_PREFIX, CONTEXT_PROPERTY_SUFFIX), value);

        } else if (endsWithIgnoreCase(key, ADDITIVITY_SUFFIX)) {
            logger(loggerContext, key, ADDITIVITY_SUFFIX).setAdditive(toBoolean(value));

        } else if (endsWithIgnoreCase(key, LEVEL_SUFFIX)) {
            logger(loggerContext, key, LEVEL_SUFFIX).setLevel(toLevel(value));
        }
    }
}
