package com.github.charlemaznable.logback.dendrobe.effect;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class EffectiveLevel {

    private Level level;
    private int effectiveLevelInt = Level.DEBUG_INT;

    public EffectiveLevel init(int effectiveLevelInt) {
        this.effectiveLevelInt = effectiveLevelInt;
        return this;
    }
}
