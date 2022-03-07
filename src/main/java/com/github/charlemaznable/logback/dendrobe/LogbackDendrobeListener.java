package com.github.charlemaznable.logback.dendrobe;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.COWArrayList;
import com.github.charlemaznable.core.config.impl.PropertiesConfigLoader;
import com.github.charlemaznable.logback.dendrobe.configurator.AppenderConfigurator;
import com.github.charlemaznable.logback.dendrobe.configurator.Configurator;
import com.github.charlemaznable.logback.dendrobe.effect.EffectorContext;
import com.github.charlemaznable.logback.dendrobe.effect.EffectorTurboFilter;
import com.google.common.base.Splitter;
import lombok.val;

import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import static ch.qos.logback.classic.ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH;
import static ch.qos.logback.classic.LoggerContext.DEFAULT_PACKAGING_DATA;
import static com.github.charlemaznable.core.lang.ClzPath.classResource;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.logback.dendrobe.console.ConsoleConfigurator.defaultConsoleAppender;
import static com.github.charlemaznable.logback.dendrobe.effect.EffectorContextElf.EFFECTOR_CONTEXT;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public final class LogbackDendrobeListener implements LoggerContextListener {

    private static final String PACKAGING_DATA_ENABLED_KEY = "context.packagingDataEnabled";
    private static final String MAX_CALLER_DATA_DEPTH_KEY = "context.maxCallerDataDepth";
    private static final String FRAMEWORK_PACKAGES_KEY = "context.frameworkPackages";

    private static final String SPRING_BOOT_LOGGING_SYSTEM = "org.springframework.boot.logging.LoggingSystem";

    private static ServiceLoader<Configurator> configurators;
    private static HotUpdater hotUpdater;

    static {
        configurators = ServiceLoader.load(Configurator.class);
        hotUpdater = findHotUpdater();
    }

    private Properties defaults;
    private Properties config;
    private LoggerContext loggerContext;
    private EffectorContext effectorContext;
    private EffectorTurboFilter effectorTurboFilter;

    public static Properties rebuildProperties(Properties properties) {
        val result = new Properties();
        properties.stringPropertyNames().forEach(key ->
                // ROOT logger's name is ignored case
                result.setProperty(startsWithIgnoreCase(key, ROOT_LOGGER_NAME)
                        ? key.toUpperCase() : key, properties.getProperty(key))
        );
        return result;
    }

    private static HotUpdater findHotUpdater() {
        val hotUpdaters = ServiceLoader.load(HotUpdater.class).iterator();
        if (!hotUpdaters.hasNext()) return new DefaultHotUpdater();

        val result = hotUpdaters.next();
        if (hotUpdaters.hasNext())
            throw new IllegalStateException("Multiple HotUpdater Defined");
        return result;
    }

    public LogbackDendrobeListener() {
        // 读取本地配置
        this.defaults = loadDefaults();
        // 本地配置作为默认配置
        this.config = new Properties(this.defaults);

        hotUpdater.initialize(this, this.config);
    }

    public void reset(Properties properties) {
        // 重置为本地默认配置
        this.config = new Properties(this.defaults);
        // 重置配置覆盖默认配置
        this.config.putAll(rebuildProperties(properties));

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

    private Properties loadDefaults() {
        val localConfigURL = classResource("logback-dendrobe.properties");
        return rebuildProperties(checkNull(localConfigURL, Properties::new,
                url -> new PropertiesConfigLoader().loadConfigable(url).getProperties()));
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

            config.stringPropertyNames().forEach(key ->
                    configurators.forEach(configurator ->
                            configurator.configurate(loggerContext,
                                    key, config.getProperty(key, "")))
            );

            val appenders = new COWArrayList<Appender>(new Appender[0]);
            configurators.forEach(configurator -> {
                configurator.postConfigurate(loggerContext);

                if (configurator instanceof AppenderConfigurator) {
                    val ac = (AppenderConfigurator) configurator;
                    val appenderList = ac.getAppenderList();
                    appenders.addAll(appenderList);
                    ac.clearAppenderList();
                }
            });

            if (appenders.isEmpty()) {
                appenders.add(defaultConsoleAppender(loggerContext));
            }
            appenders.forEach(LifeCycle::start);
        }
    }

    // package access for test
    String getRaw(String key) {
        return config.getProperty(key);
    }

    @SuppressWarnings("SameParameterValue")
    private boolean getBool(String key, boolean defValue) {
        return toBoolean(getRaw(key));
    }

    @SuppressWarnings("SameParameterValue")
    private int getInt(String key, int defValue) {
        return toInt(getRaw(key), defValue);
    }

    @SuppressWarnings("SameParameterValue")
    private List<String> getList(String key) {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(toStr(getRaw(key)));
    }

    private static class DefaultHotUpdater implements HotUpdater {

        @Override
        public void initialize(LogbackDendrobeListener listener, Properties config) {
            // empty method
        }
    }
}
