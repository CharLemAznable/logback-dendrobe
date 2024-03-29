package com.github.charlemaznable.logback.dendrobe.effect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilderElf.builders;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Effector {

    private final Logger logger;
    private final Effector parent;
    private final LoggerContext loggerContext;
    private List<Effector> childrenList;
    private final Map<String, EffectiveLevel> effectiveLevelMap = new ConcurrentSkipListMap<>();

    public Effector(Logger logger, Effector parent, LoggerContext loggerContext) {
        this.logger = logger;
        this.parent = parent;
        this.loggerContext = loggerContext;

        val loggerLevel = logger.getLevel();
        builders().forEach(builder -> {
            val effectorName = builder.effectorName();
            this.effectiveLevelMap.put(effectorName, builder.init(isNull(loggerLevel)
                    ? parent.getEffectorLevelInt(effectorName) : loggerLevel.levelInt));
        });
    }

    public Level getLoggerLevel() {
        return logger.getLevel();
    }

    public void setLoggerLevel(Level level) {
        logger.setLevel(level);

        this.effectiveLevelMap.forEach((effectorName, effectiveLevel) -> {
            if (isNull(effectiveLevel.getLevel())) {
                setEffectiveLevel(effectorName, null);
            }
        });
    }

    public Level getEffectorLevel(String effectorName) {
        return getEffectiveLevel(effectorName).getLevel();
    }

    public int getEffectorLevelInt(String effectorName) {
        return getEffectiveLevel(effectorName).getEffectiveLevelInt();
    }

    public synchronized void setEffectorLevel(String effectorName, Level effectorLevel) {
        val effectiveLevel = getEffectiveLevel(effectorName);
        if (effectiveLevel.getLevel() == effectorLevel) return;
        effectiveLevel.setLevel(effectorLevel);
        setEffectiveLevel(effectorName, effectorLevel);
    }

    public boolean isGreaterThanLevel(Level level) {
        for (val entry : this.effectiveLevelMap.entrySet()) {
            if (entry.getValue().getEffectiveLevelInt()
                    <= level.levelInt) return false;
        }
        return true;
    }

    private void setEffectiveLevel(String effectorName, Level effectorLevel) {
        val effectiveLevel = getEffectiveLevel(effectorName);
        if (isNull(effectorLevel)) {
            val loggerLevel = getLoggerLevel();
            effectiveLevel.init(isNull(loggerLevel)
                    ? parent.getEffectorLevelInt(effectorName)
                    : loggerLevel.levelInt);
        } else {
            effectiveLevel.setEffectiveLevelInt(effectorLevel.levelInt);
        }

        if (nonNull(childrenList)) {
            for (val child : childrenList) {
                child.handleParentEffectiveLevelChange(effectorName,
                        effectiveLevel.getEffectiveLevelInt());
            }
        }
    }

    private synchronized void handleParentEffectiveLevelChange(
            String effectorName, int newParentEffectiveLevelInt) {
        val effectiveLevel = getEffectiveLevel(effectorName);
        if (isNull(effectiveLevel.getLevel()) && isNull(getLoggerLevel())) {
            effectiveLevel.setEffectiveLevelInt(newParentEffectiveLevelInt);

            if (nonNull(childrenList)) {
                for (val child : childrenList) {
                    child.handleParentEffectiveLevelChange(
                            effectorName, newParentEffectiveLevelInt);
                }
            }
        }
    }

    private EffectiveLevel getEffectiveLevel(String effectorName) {
        return checkNotNull(this.effectiveLevelMap.get(effectorName),
                "Unknown Effector Name: " + effectorName);
    }

    Effector getChildByName(final String childName) {
        if (isNull(childrenList)) return null;
        for (val childEffector : childrenList) {
            if (childName.equals(childEffector.logger.getName())) {
                return childEffector;
            }
        }
        return null;
    }

    Effector createChildByName(final String childName) {
        if (isNull(childrenList)) childrenList = new CopyOnWriteArrayList<>();
        val childLogger = loggerContext.getLogger(childName);
        val childEffector = new Effector(childLogger, this, loggerContext);
        childrenList.add(childEffector);
        return childEffector;
    }

    void recursiveReset() {
        builders().forEach(builder -> this.effectiveLevelMap.put(
                builder.effectorName(), builder.build()));

        if (childrenList == null) return;
        for (val child : childrenList) {
            child.recursiveReset();
        }
    }
}
