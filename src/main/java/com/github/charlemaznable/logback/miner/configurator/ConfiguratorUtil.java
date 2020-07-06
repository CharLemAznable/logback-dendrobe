package com.github.charlemaznable.logback.miner.configurator;

public class ConfiguratorUtil {

    private ConfiguratorUtil() {}

    public static String fetchPropertyKey(String key, String prefix, String suffix) {
        return key.substring(prefix.length(), key.length() - suffix.length());
    }

    public static String fetchLoggerName(String key, String suffix) {
        return key.substring(0, key.length() - suffix.length());
    }
}
