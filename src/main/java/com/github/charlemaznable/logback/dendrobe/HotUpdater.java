package com.github.charlemaznable.logback.dendrobe;

import java.util.Properties;

public interface HotUpdater {

    void initialize(LogbackDendrobeListener listener, Properties config);
}
