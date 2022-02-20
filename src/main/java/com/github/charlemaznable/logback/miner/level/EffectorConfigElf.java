package com.github.charlemaznable.logback.miner.level;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EffectorConfigElf {

    private static ServiceLoader<EffectorBuilder> configurators;

    static {
        configurators = ServiceLoader.load(EffectorBuilder.class);
    }

    static ServiceLoader<EffectorBuilder> configurators() {
        return configurators;
    }
}
