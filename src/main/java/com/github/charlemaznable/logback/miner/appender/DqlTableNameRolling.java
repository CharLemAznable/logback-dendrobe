package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import lombok.Getter;
import lombok.val;
import org.n3r.eql.diamond.Dql;

import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackRollingSqlCache.useLogbackRollingSql;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class DqlTableNameRolling {

    public static final String ACTIVE_TABLE_NAME = "activeTableName";

    private final Object rollingLock = new Object();
    private FileNamePattern tableNamePattern;
    private RollingCalendar rollingCalendar;
    private Date dateInCurrentPeriod = new Date(0);
    private long nextCheck = 0;
    private boolean enabled = false;
    @Getter
    private String activeTableName;

    public DqlTableNameRolling(String tableNamePatternStr, Context context) {
        if (isBlank(tableNamePatternStr)) return;

        val pattern = new FileNamePattern(tableNamePatternStr, context);
        val dateTokenConverter = pattern.getPrimaryDateTokenConverter();
        val hasDateToken = nonNull(dateTokenConverter);
        val hasIntegerToken = nonNull(pattern.getIntegerTokenConverter());
        if (!hasDateToken || hasIntegerToken) return;

        RollingCalendar rc;
        if (dateTokenConverter.getTimeZone() != null) {
            rc = new RollingCalendar(dateTokenConverter.getDatePattern(),
                    dateTokenConverter.getTimeZone(), Locale.getDefault());
        } else {
            rc = new RollingCalendar(dateTokenConverter.getDatePattern());
        }
        if (!rc.isCollisionFree()) return;

        enabled = true;
        tableNamePattern = pattern;
        rollingCalendar = rc;
    }

    public void rolling(Dql dql, String prepareSql) {
        rolling(tableName -> {
            if (isBlank(prepareSql)) return;
            val currentMap = newHashMap();
            currentMap.put(ACTIVE_TABLE_NAME, tableName);
            dql.params(currentMap).dynamics(currentMap).execute(prepareSql);
        });
    }

    public void rolling(Dql dql, Class<?> clazz) {
        rolling(tableName -> {
            if (!useLogbackRollingSql(clazz, dql)) return;
            val currentMap = newHashMap();
            currentMap.put(ACTIVE_TABLE_NAME, tableName);
            dql.params(currentMap).dynamics(currentMap).execute();
        });
    }

    public void rolling(Consumer<String> rollover) {
        synchronized (rollingLock) {
            if (isTrigging()) requireNonNull(rollover).accept(activeTableName);
        }
    }

    private boolean isTrigging() {
        if (!enabled) return false;
        long time = currentTimeMillis();
        if (time >= nextCheck) {
            setDateInCurrentPeriod(time);
            computeNextCheck();
            computeActiveTableName();
            return true;
        } else {
            return false;
        }
    }

    private void setDateInCurrentPeriod(long now) {
        dateInCurrentPeriod.setTime(now);
    }

    private void computeNextCheck() {
        nextCheck = rollingCalendar.getNextTriggeringDate(dateInCurrentPeriod).getTime();
    }

    private void computeActiveTableName() {
        activeTableName = tableNamePattern.convert(dateInCurrentPeriod);
    }
}
