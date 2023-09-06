package com.github.charlemaznable.logback.dendrobe.effect;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EffectorBuilderElf {

    private static final CopyOnWriteArrayList<EffectorBuilder> builders;

    static {
        builders = StreamSupport
                .stream(ServiceLoader.load(EffectorBuilder.class).spliterator(), false)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    static List<EffectorBuilder> builders() {
        return builders;
    }
}
