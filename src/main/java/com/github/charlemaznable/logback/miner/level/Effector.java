package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Effector {

    private final Logger logger;

    private final Effector parent;
    private List<Effector> childrenList;
    private final LoggerContext loggerContext;
    // console
    @Getter
    private Level consoleLevel;
    @Getter
    private int consoleEffectiveLevelInt;
    // dql
    @Getter
    private Level dqlLevel;
    @Getter
    private int dqlEffectiveLevelInt;

    public Effector(Logger logger, Effector parent, LoggerContext loggerContext) {
        this.logger = logger;
        this.parent = parent;
        this.loggerContext = loggerContext;

        this.consoleEffectiveLevelInt = calcConsoleEffectiveLevelInt(logger.getLevel());
        this.dqlEffectiveLevelInt = calcDqlEffectiveLevelInt(logger.getLevel());
    }

    public Level getLoggerLevel() {
        return logger.getLevel();
    }

    public void setLoggerLevel(Level level) {
        logger.setLevel(level);

        if (isNull(consoleLevel)) {
            consoleEffectiveLevelInt = calcConsoleEffectiveLevelInt(level);

            if (nonNull(childrenList)) {
                for (val child : childrenList) {
                    child.handleParentConsoleLevelChange(consoleEffectiveLevelInt);
                }
            }
        }
        if (isNull(dqlLevel)) {
            dqlEffectiveLevelInt = calcDqlEffectiveLevelInt(level);

            if (nonNull(childrenList)) {
                for (val child : childrenList) {
                    child.handleParentDqlLevelChange(dqlEffectiveLevelInt);
                }
            }
        }
    }

    public synchronized void setConsoleLevel(Level consoleLevel) {
        if (this.consoleLevel == consoleLevel) return;

        this.consoleLevel = consoleLevel;
        if (isNull(consoleLevel)) {
            consoleEffectiveLevelInt = calcConsoleEffectiveLevelInt(getLoggerLevel());
        } else {
            consoleEffectiveLevelInt = consoleLevel.levelInt;
        }

        if (nonNull(childrenList)) {
            for (val child : childrenList) {
                child.handleParentConsoleLevelChange(consoleEffectiveLevelInt);
            }
        }
    }

    public synchronized void setDqlLevel(Level dqlLevel) {
        if (this.dqlLevel == dqlLevel) return;

        this.dqlLevel = dqlLevel;
        if (isNull(dqlLevel)) {
            dqlEffectiveLevelInt = calcDqlEffectiveLevelInt(getLoggerLevel());
        } else {
            dqlEffectiveLevelInt = dqlLevel.levelInt;
        }

        if (nonNull(childrenList)) {
            for (val child : childrenList) {
                child.handleParentDqlLevelChange(dqlEffectiveLevelInt);
            }
        }
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
        consoleEffectiveLevelInt = Level.DEBUG_INT;
        consoleLevel = null;
        dqlEffectiveLevelInt = Level.DEBUG_INT;
        dqlLevel = null;

        if (childrenList == null) return;
        for (val child : childrenList) {
            child.recursiveReset();
        }
    }

    private int calcConsoleEffectiveLevelInt(Level loggerLevel) {
        return isNull(loggerLevel) ? parent.consoleEffectiveLevelInt : loggerLevel.levelInt;
    }

    private int calcDqlEffectiveLevelInt(Level loggerLevel) {
        return isNull(loggerLevel) ? parent.dqlEffectiveLevelInt : loggerLevel.levelInt;
    }

    private synchronized void handleParentConsoleLevelChange(int newParentConsoleLevelInt) {
        if (isNull(consoleLevel) && isNull(getLoggerLevel())) {
            consoleEffectiveLevelInt = newParentConsoleLevelInt;

            if (nonNull(childrenList)) {
                for (val child : childrenList) {
                    child.handleParentConsoleLevelChange(newParentConsoleLevelInt);
                }
            }
        }
    }

    private synchronized void handleParentDqlLevelChange(int newParentDqlLevelInt) {
        if (isNull(dqlLevel) && isNull(getLoggerLevel())) {
            dqlEffectiveLevelInt = newParentDqlLevelInt;

            if (nonNull(childrenList)) {
                for (val child : childrenList) {
                    child.handleParentDqlLevelChange(newParentDqlLevelInt);
                }
            }
        }
    }
}
