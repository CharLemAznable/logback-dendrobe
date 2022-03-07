package com.github.charlemaznable.logback.dendrobe.effect;

public interface EffectorBuilder {

    String effectorName();

    default EffectiveLevel build() {
        return new EffectiveLevel();
    }

    default EffectiveLevel init(int effectiveLevelInt) {
        return build().init(effectiveLevelInt);
    }
}
