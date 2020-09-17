package com.github.charlemaznable.logback.miner.appender;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.SneakyThrows;
import lombok.val;
import org.slf4j.helpers.Util;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

class VertxElf {

    @SneakyThrows
    static Vertx buildVertx(VertxOptions vertxOptions) {
        if (vertxOptions.getEventBusOptions().isClustered()) {
            val completableFuture = new CompletableFuture<Vertx>();
            Vertx.clusteredVertx(vertxOptions, asyncResult -> {
                if (asyncResult.failed()) {
                    completableFuture.completeExceptionally(asyncResult.cause());
                } else {
                    completableFuture.complete(asyncResult.result());
                }
            });
            return completableFuture.exceptionally(throwable -> {
                Util.report("Building Vertx failed", throwable);
                return null;
            }).get();
        } else {
            return Vertx.vertx(vertxOptions);
        }
    }

    static void closeVertxQuietly(Vertx vertx) {
        if (isNull(vertx)) return;
        vertx.close();
    }

    @SneakyThrows
    static void closeVertx(Vertx vertx) {
        if (isNull(vertx)) return;

        val completableFuture = new CompletableFuture<Void>();
        vertx.close(asyncResult -> {
            if (asyncResult.failed()) {
                completableFuture.completeExceptionally(asyncResult.cause());
            } else {
                completableFuture.complete(asyncResult.result());
            }
        });
        completableFuture.exceptionally(throwable -> {
            Util.report("Closing Vertx failed", throwable);
            return null;
        }).get();
    }

    private VertxElf() {}
}
