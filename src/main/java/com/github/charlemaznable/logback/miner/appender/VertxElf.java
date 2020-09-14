package com.github.charlemaznable.logback.miner.appender;

import com.google.common.primitives.Primitives;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.util.O;
import org.n3r.eql.util.O.ValueGettable;
import org.slf4j.helpers.Util;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.parseObject;
import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;

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

    static VertxOptions parseStoneToVertxOptions(String stone) {
        val vertxOptions = new VertxOptions();

        val properties = parseStoneToProperties(stone);
        for (val prop : properties.entrySet()) {
            O.setValue(vertxOptions, Objects.toString(prop.getKey()), new ValueGettable() {
                @Override
                public Object getValue() {
                    return prop.getValue();
                }

                @SuppressWarnings("unchecked")
                @Override
                public Object getValue(Class<?> returnType) {
                    val value = Objects.toString(prop.getValue());
                    val rt = Primitives.unwrap(returnType);
                    if (rt == String.class) return value;
                    if (rt.isPrimitive()) return parsePrimitive(rt, value);

                    if (Enum.class.isAssignableFrom(returnType)) {
                        try {
                            return Enum.valueOf((Class<Enum>) returnType, value);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return parseObject(value, rt);
                }
            });
        }

        return vertxOptions;
    }

    @SuppressWarnings("Duplicates")
    private static Object parsePrimitive(Class<?> rt, String value) {
        if (rt == boolean.class) return equalsAnyIgnoreCase(value, "yes", "true", "on", "y");
        if (rt == short.class) return Short.parseShort(value);
        if (rt == int.class) return Integer.parseInt(value);
        if (rt == long.class) return Long.parseLong(value);
        if (rt == float.class) return Float.parseFloat(value);
        if (rt == double.class) return Double.parseDouble(value);
        if (rt == byte.class) return Byte.parseByte(value);
        if (rt == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
        return null;
    }

    private VertxElf() {}
}
