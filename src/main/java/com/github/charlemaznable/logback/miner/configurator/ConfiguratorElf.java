package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.NoArgsConstructor;
import lombok.val;

import static com.github.charlemaznable.logback.miner.level.EffectorContextElf.getEffectorContext;
import static com.google.common.base.Preconditions.checkNotNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ConfiguratorElf {

    static String propertyKey(String key, String prefix, String suffix) {
        return key.substring(prefix.length(), key.length() - suffix.length());
    }

    static String loggerName(String key, String suffix) {
        return key.substring(0, key.length() - suffix.length());
    }

    public static Logger logger(LoggerContext loggerContext, String name) {
        return loggerContext.getLogger(name);
    }

    public static Logger logger(LoggerContext loggerContext, String key, String suffix) {
        return logger(loggerContext, loggerName(key, suffix));
    }

    public static Effector effector(LoggerContext loggerContext, String key, String suffix) {
        val effectorContext = checkNotNull(getEffectorContext(loggerContext));
        return effectorContext.getEffector(loggerName(key, suffix));
    }
}
