package com.github.charlemaznable.logback.dendrobe.vertx;

import com.github.charlemaznable.logback.dendrobe.impl.DefaultVertxOptionsService;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class VertxOptionsServiceElf {

    private static final VertxOptionsService optionsService;

    static {
        optionsService = findVertxOptionsService();
    }

    public static VertxOptionsService optionsService() {
        return optionsService;
    }

    private static VertxOptionsService findVertxOptionsService() {
        val optionsServices = ServiceLoader.load(VertxOptionsService.class).iterator();
        if (!optionsServices.hasNext()) return new DefaultVertxOptionsService();

        val result = optionsServices.next();
        if (optionsServices.hasNext())
            throw new IllegalStateException("Multiple VertxOptionsService Defined");
        return result;
    }
}
