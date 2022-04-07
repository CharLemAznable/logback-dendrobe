package com.github.charlemaznable.logback.dendrobe.es;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Objectt.setValue;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

@Getter
@Setter
public class EsBatchConfig {

    public static final int DEFAULT_MAX_BATCH_SIZE = 1024;
    public static final int DEFAULT_INITIAL_DELAY = 4;
    public static final int DEFAULT_DELAY = 4;
    public static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private long initialDelay = DEFAULT_INITIAL_DELAY;
    private long delay = DEFAULT_DELAY;
    private TimeUnit unit = DEFAULT_UNIT;

    public static EsBatchConfig parsePropertiesToEsBatchConfig(Properties properties) {
        val esBatchConfig = new EsBatchConfig();
        for (val prop : properties.entrySet()) {
            setValue(esBatchConfig, Objects.toString(prop.getKey()), returnType -> {
                val value = Objects.toString(prop.getValue());
                val rt = Primitives.unwrap(checkNotNull(returnType));
                if (rt == int.class) return toInt(value);
                if (rt == long.class) return toLong(value);
                if (rt == TimeUnit.class) return TimeUnit.valueOf(value);
                return null;
            });
        }
        return esBatchConfig;
    }
}
