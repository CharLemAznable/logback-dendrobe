package com.github.charlemaznable.logback.dendrobe.file;

import com.github.charlemaznable.logback.dendrobe.effect.EffectorBuilder;
import com.google.auto.service.AutoService;

@AutoService(EffectorBuilder.class)
public final class FileEffectorBuilder implements EffectorBuilder {

    public static final String FILE_EFFECTOR = "FILE_EFFECTOR";

    @Override
    public String effectorName() {
        return FILE_EFFECTOR;
    }
}
