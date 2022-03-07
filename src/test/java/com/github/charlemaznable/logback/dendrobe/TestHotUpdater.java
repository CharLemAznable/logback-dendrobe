package com.github.charlemaznable.logback.dendrobe;

import com.google.auto.service.AutoService;

import java.util.Properties;

@AutoService(HotUpdater.class)
public class TestHotUpdater implements HotUpdater {

    private static LogbackDendrobeListener listener;

    public static LogbackDendrobeListener listener() {
        return listener;
    }

    @Override
    public void initialize(LogbackDendrobeListener listener, Properties config) {
        TestHotUpdater.listener = listener;
    }
}
