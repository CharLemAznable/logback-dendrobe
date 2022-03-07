package com.github.charlemaznable.logback.dendrobe.effect;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LoggerNameUtil;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public final class EffectorContext {

    private Effector root;
    private Map<String, Effector> effectorCache = new ConcurrentHashMap<>();

    public EffectorContext(LoggerContext loggerContext) {
        this.root = new Effector(loggerContext
                .getLogger(ROOT_LOGGER_NAME), null, loggerContext);
        this.effectorCache.put(ROOT_LOGGER_NAME, this.root);
    }

    @SuppressWarnings("Duplicates")
    public Effector getEffector(String name) {
        if (name == null) throw new IllegalArgumentException("name argument cannot be null");
        if (ROOT_LOGGER_NAME.equalsIgnoreCase(name)) return root;

        int i = 0;
        Effector effector = root;
        Effector childEffector = effectorCache.get(name);
        if (childEffector != null) return childEffector;

        String childName;
        while (true) {
            val h = LoggerNameUtil.getSeparatorIndexOf(name, i);
            if (h == -1) {
                childName = name;
            } else {
                childName = name.substring(0, h);
            }
            i = h + 1;
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (effector) {
                childEffector = effector.getChildByName(childName);
                if (childEffector == null) {
                    childEffector = effector.createChildByName(childName);
                    effectorCache.put(childName, childEffector);
                }
            }
            effector = childEffector;
            if (h == -1) {
                return childEffector;
            }
        }
    }

    public void reset() {
        root.recursiveReset();
    }
}
