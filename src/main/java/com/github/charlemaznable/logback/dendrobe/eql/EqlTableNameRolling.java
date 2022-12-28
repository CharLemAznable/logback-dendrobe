package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import lombok.Getter;
import lombok.val;
import org.n3r.eql.Eql;

import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.logback.dendrobe.eql.EqlCaches.EqlLogRollingSqlCache.useEqlLogRollingSql;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;

public final class EqlTableNameRolling {

    public static final String ACTIVE_TABLE_NAME = "activeTableName";

    private final Object rollingLock = new Object();
    private FileNamePattern tableNamePattern;
    private RollingCalendar rollingCalendar;
    private final Date dateInCurrentPeriod = new Date(0);
    private long nextCheck = 0;
    private boolean enabled = false;
    @Getter
    private String activeTableName;

    public EqlTableNameRolling(String tableNamePatternStr, Context context) {
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

    public void rolling(Eql eql, String prepareSql) {
        rolling(tableName -> {
            if (isBlank(prepareSql)) return;
            val currentMap = of(ACTIVE_TABLE_NAME, tableName);
            eql.params(currentMap).dynamics(currentMap).execute(prepareSql);
        });
    }

    public void rolling(Eql eql, Class<?> clazz) {
        rolling(tableName -> {
            if (!useEqlLogRollingSql(clazz, eql)) return;
            val currentMap = of(ACTIVE_TABLE_NAME, tableName);
            eql.params(currentMap).dynamics(currentMap).execute();
        });
    }

    public void rolling(Consumer<String> rollover) {
        synchronized (rollingLock) {
            if (isTrigging()) checkNotNull(rollover).accept(activeTableName);
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
