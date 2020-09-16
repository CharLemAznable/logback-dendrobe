package com.github.charlemaznable.logback.miner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import com.github.charlemaznable.logback.miner.configurator.Configurator;
import com.github.charlemaznable.logback.miner.level.EffectorContext;
import com.github.charlemaznable.logback.miner.level.EffectorTurboFilter;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.val;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.impl.DiamondSubscriber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import static ch.qos.logback.classic.ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH;
import static ch.qos.logback.classic.LoggerContext.DEFAULT_PACKAGING_DATA;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.EFFECTOR_CONTEXT;
import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class LogbackMinerDiamondListener implements DiamondListener, LoggerContextListener {

    private static final String DIAMOND_GROUP_KEY = "logback.miner.group";
    private static final String DIAMOND_DATA_ID_KEY = "logback.miner.dataId";

    private static final String DEFAULT_GROUP = "Logback";
    private static final String DEFAULT_DATA_ID = "default";

    private static final String PACKAGING_DATA_ENABLED_KEY = "context.packagingDataEnabled";
    private static final String MAX_CALLER_DATA_DEPTH_KEY = "context.maxCallerDataDepth";
    private static final String FRAMEWORK_PACKAGES_KEY = "context.frameworkPackages";

    private static final String SPRING_BOOT_LOGGING_SYSTEM = "org.springframework.boot.logging.LoggingSystem";

    private static ServiceLoader<Configurator> configurators;

    private Properties defaultConfig;
    private Properties minerConfig;

    private LoggerContext loggerContext;
    private EffectorContext effectorContext;
    private EffectorTurboFilter effectorTurboFilter;

    @Getter
    private volatile boolean listening;

    static {
        configurators = ServiceLoader.load(Configurator.class);
    }

    public LogbackMinerDiamondListener() {
        // 读取本地配置
        this.defaultConfig = loadLocalConfig();
        // 本地配置作为默认配置
        this.minerConfig = new Properties(this.defaultConfig);
        // 本地配置diamond配置坐标
        val group = this.defaultConfig.getProperty(DIAMOND_GROUP_KEY, DEFAULT_GROUP);
        val dataId = this.defaultConfig.getProperty(DIAMOND_DATA_ID_KEY, DEFAULT_DATA_ID);

        new Thread(() -> {
            // diamond配置覆盖默认配置
            val diamondAxis = DiamondAxis.makeAxis(group, dataId);
            val diamondStone = new DiamondStone();
            diamondStone.setContent(new Miner().getStone(group, dataId));
            diamondStone.setDiamondAxis(diamondAxis);
            accept(diamondStone);

            DiamondSubscriber.getInstance().addDiamondListener(
                    diamondAxis, LogbackMinerDiamondListener.this);
            listening = true;
        }).start();
    }

    @Override
    public void accept(DiamondStone diamondStone) {
        // 重置为本地默认配置
        minerConfig = new Properties(defaultConfig);
        // diamond配置覆盖默认配置
        minerConfig.putAll(rebuildProperties(
                parseStoneToProperties(diamondStone.getContent())));

        resetLoggerContext(loggerContext);
        configureLoggerContext(loggerContext);
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
        // empty method
    }

    @Override
    public void onStop(LoggerContext context) {
        // empty method
    }

    @Override
    public void onReset(LoggerContext context) {
        effectorContext.reset();
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
        effectorContext.getEffector(logger.getName()).setLoggerLevel(level);
    }

    private Properties loadLocalConfig() {
        val result = new Properties();
        val localConfigURL = currentThread().getContextClassLoader()
                .getResource("logback-miner.properties");
        if (nonNull(localConfigURL)) {
            InputStream inputStream = null;
            try {
                inputStream = localConfigURL.openStream();
                result.load(inputStream);
            } catch (IOException ignored) {
                // ignored
            } finally {
                closeQuietly(inputStream);
            }
        }
        return rebuildProperties(result);
    }

    private Properties rebuildProperties(Properties properties) {
        val result = new Properties();

        for (val key : properties.stringPropertyNames()) {
            // ROOT logger's name is ignored case
            result.setProperty(startsWithIgnoreCase(key, ROOT_LOGGER_NAME)
                    ? key.toUpperCase() : key, properties.getProperty(key));
        }
        return result;
    }

    // package access for configurator
    void initLoggerContext(LoggerContext loggerContext) {
        if (isNull(loggerContext)) return;

        synchronized (loggerContext.getConfigurationLock()) {
            loggerContext.addListener(this);
            // LogbackLoggingSystem#markAsInitialized
            loggerContext.putObject(SPRING_BOOT_LOGGING_SYSTEM, new Object());

            this.loggerContext = loggerContext;
            this.effectorContext = new EffectorContext(loggerContext);
            this.effectorTurboFilter = new EffectorTurboFilter(effectorContext);
        }
    }

    private void resetLoggerContext(LoggerContext loggerContext) {
        if (isNull(loggerContext)) return;

        synchronized (loggerContext.getConfigurationLock()) {
            loggerContext.setPackagingDataEnabled(DEFAULT_PACKAGING_DATA);
            loggerContext.setMaxCallerDataDepth(DEFAULT_MAX_CALLEDER_DATA_DEPTH);
            loggerContext.getFrameworkPackages().clear();
            loggerContext.reset();
            // LogbackLoggingSystem#markAsInitialized
            loggerContext.putObject(SPRING_BOOT_LOGGING_SYSTEM, new Object());
        }
    }

    // package access for configurator
    void configureLoggerContext(LoggerContext loggerContext) {
        if (isNull(loggerContext)) return;

        synchronized (loggerContext.getConfigurationLock()) {
            loggerContext.putObject(EFFECTOR_CONTEXT, effectorContext);
            loggerContext.addTurboFilter(effectorTurboFilter);

            loggerContext.setPackagingDataEnabled(
                    getBool(PACKAGING_DATA_ENABLED_KEY, DEFAULT_PACKAGING_DATA));
            loggerContext.setMaxCallerDataDepth(
                    getInt(MAX_CALLER_DATA_DEPTH_KEY, DEFAULT_MAX_CALLEDER_DATA_DEPTH));
            loggerContext.getFrameworkPackages().addAll(getList(FRAMEWORK_PACKAGES_KEY));

            for (val configurator : configurators) {
                configurator.before(loggerContext);
            }

            for (val key : minerConfig.stringPropertyNames()) {
                val value = minerConfig.getProperty(key, "");
                for (val configurator : configurators) {
                    configurator.configurate(loggerContext, key, value);
                }
            }

            for (val configurator : configurators) {
                configurator.finish(loggerContext);
            }
        }
    }

    // package access for test
    String getRaw(String key) {
        return minerConfig.getProperty(key);
    }

    @SuppressWarnings("SameParameterValue")
    private boolean getBool(String key, boolean defValue) {
        val raw = getRaw(key);
        if (isBlank(raw)) return defValue;
        return toBool(raw);
    }

    @SuppressWarnings("SameParameterValue")
    private int getInt(String key, int defValue) {
        val raw = getRaw(key);
        if (isBlank(raw)) return defValue;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private List<String> getList(String key) {
        val raw = getRaw(key);
        if (isBlank(raw)) return new ArrayList<>();
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(raw);
    }
}
