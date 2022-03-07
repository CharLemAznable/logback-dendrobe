package com.github.charlemaznable.logback.dendrobe.vertx;

import io.vertx.core.VertxOptions;

public interface VertxOptionsService {

    String getVertxOptionsValue(String configKey);

    VertxOptions parseVertxOptions(String configKey, String configValue);
}
