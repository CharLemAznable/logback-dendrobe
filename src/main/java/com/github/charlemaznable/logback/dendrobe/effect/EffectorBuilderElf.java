package com.github.charlemaznable.logback.dendrobe.effect;

import lombok.NoArgsConstructor;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EffectorBuilderElf {

    private static ServiceLoader<EffectorBuilder> builders;

    static {
        builders = ServiceLoader.load(EffectorBuilder.class);
    }

    static ServiceLoader<EffectorBuilder> builders() {
        return builders;
    }
}
