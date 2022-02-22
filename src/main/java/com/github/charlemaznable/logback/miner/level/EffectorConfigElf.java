package com.github.charlemaznable.logback.miner.level;

import lombok.NoArgsConstructor;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EffectorConfigElf {

    private static ServiceLoader<EffectorBuilder> configurators;

    static {
        configurators = ServiceLoader.load(EffectorBuilder.class);
    }

    static ServiceLoader<EffectorBuilder> configurators() {
        return configurators;
    }
}
