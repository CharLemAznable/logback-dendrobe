package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import lombok.NoArgsConstructor;
import lombok.val;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ThrowableProxyElf {

    public static String toString(Throwable t) {
        return toString(new ThrowableProxy(t));
    }

    public static String toString(IThrowableProxy tp) {
        if (isNull(tp)) return "";
        val builder = new StringBuilder().append(tp.getClassName()).append(": ")
                .append(tp.getMessage()).append(CoreConstants.LINE_SEPARATOR);
        for (val step : tp.getStackTraceElementProxyArray()) {
            builder.append(CoreConstants.TAB).append(step.toString());
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            builder.append(CoreConstants.LINE_SEPARATOR);
        }
        return builder.toString();
    }
}
