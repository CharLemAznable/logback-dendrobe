package com.github.charlemaznable.logback.miner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;
import com.github.charlemaznable.logback.miner.appender.ConsoleAppender;
import com.github.charlemaznable.logback.miner.level.EffectorContext;
import com.github.charlemaznable.logback.miner.turbo.LogbackMinerFilter;
import com.google.common.base.Splitter;
import lombok.val;
import lombok.var;
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

import static ch.qos.logback.classic.ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH;
import static ch.qos.logback.classic.LoggerContext.DEFAULT_PACKAGING_DATA;
import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class LogbackMinerDiamondListener implements DiamondListener, LoggerContextListener {

    private static final String DIAMOND_GROUP_KEY = "logback.miner.group";
    private static final String DIAMOND_DATA_ID_KEY = "logback.miner.dataId";

    private static final String DEFAULT_GROUP = "Logback";
    private static final String DEFAULT_DATA_ID = "default";

    private static final String[] TRUE_VALUES = new String[]{"true", "yes", "on", "y"};

    private static final String PACKAGING_DATA_ENABLED_KEY = "context.packagingDataEnabled";
    private static final String MAX_CALLER_DATA_DEPTH_KEY = "context.maxCallerDataDepth";
    private static final String FRAMEWORK_PACKAGES_KEY = "context.frameworkPackages";

    private static final String CONTEXT_PROPERTY_PREFIX = "context.property.";

    private static final String ADDITIVITY_SUFFIX = ".additivity";
    private static final String LEVEL_SUFFIX = ".level";

    private static final String CONSOLE_LEVEL_SUFFIX = ".console-level";
    private static final String CONSOLE_CHARSET_SUFFIX = ".console-charset";
    private static final String CONSOLE_PATTERN_SUFFIX = ".console-pattern";
    private static final String CONSOLE_TARGET_SUFFIX = ".console-target";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = ".console-immediateFlush";

    private static final String SPRING_BOOT_LOGGING_SYSTEM = "org.springframework.boot.logging.LoggingSystem";

    private Properties defaultConfig;
    private Properties minerConfig;

    private LoggerContext loggerContext;
    private EffectorContext effectorContext;
    private LogbackMinerFilter turboFilter;

    public LogbackMinerDiamondListener() {
        // 读取本地配置
        this.defaultConfig = loadLocalConfig();
        // 本地配置作为默认配置
        this.minerConfig = new Properties(this.defaultConfig);
        // 本地配置diamond配置坐标
        val group = this.defaultConfig.getProperty(DIAMOND_GROUP_KEY, DEFAULT_GROUP);
        val dataId = this.defaultConfig.getProperty(DIAMOND_DATA_ID_KEY, DEFAULT_DATA_ID);
        // diamond配置覆盖默认配置
        this.minerConfig.putAll(new Miner(group).getProperties(dataId));

        DiamondSubscriber.getInstance().addDiamondListener(
                DiamondAxis.makeAxis(group, dataId), this);
    }

    @Override
    public void accept(DiamondStone diamondStone) {
        // 重置为本地默认配置
        minerConfig = new Properties(defaultConfig);
        // diamond配置覆盖默认配置
        minerConfig.putAll(parseStoneToProperties(diamondStone.getContent()));

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
            this.effectorContext = new EffectorContext(this.loggerContext);
            this.turboFilter = new LogbackMinerFilter(this.effectorContext);
        }
    }

    private void resetLoggerContext(LoggerContext loggerContext) {
        if (isNull(loggerContext)) return;

        synchronized (loggerContext.getConfigurationLock()) {
            loggerContext.setPackagingDataEnabled(DEFAULT_PACKAGING_DATA);
            loggerContext.setMaxCallerDataDepth(DEFAULT_MAX_CALLEDER_DATA_DEPTH);
            loggerContext.getFrameworkPackages().clear();
            loggerContext.reset();
        }
    }

    // package access for configurator
    void configureLoggerContext(LoggerContext loggerContext) {
        if (isNull(loggerContext)) return;

        synchronized (loggerContext.getConfigurationLock()) {
            loggerContext.setPackagingDataEnabled(
                    getBool(PACKAGING_DATA_ENABLED_KEY, DEFAULT_PACKAGING_DATA));
            loggerContext.setMaxCallerDataDepth(
                    getInt(MAX_CALLER_DATA_DEPTH_KEY, DEFAULT_MAX_CALLEDER_DATA_DEPTH));
            loggerContext.getFrameworkPackages().addAll(getList(FRAMEWORK_PACKAGES_KEY));
            loggerContext.addTurboFilter(turboFilter);

            val appenderList = new COWArrayList<Appender>(new Appender[0]);
            appenderList.add(fetchConsoleAppender(loggerContext.getLogger(ROOT_LOGGER_NAME)));

            for (val key : minerConfig.stringPropertyNames()) {
                val value = minerConfig.getProperty(key, "");

                if (startsWithIgnoreCase(key, CONTEXT_PROPERTY_PREFIX)) {
                    val propertyKey = key.substring(CONTEXT_PROPERTY_PREFIX.length());
                    loggerContext.putProperty(propertyKey, value);

                } else if (endsWithIgnoreCase(key, ADDITIVITY_SUFFIX)) {
                    val name = fetchLoggerName(key, ADDITIVITY_SUFFIX);
                    loggerContext.getLogger(name).setAdditive(toBool(value));

                } else if (endsWithIgnoreCase(key, LEVEL_SUFFIX)) {
                    val name = fetchLoggerName(key, LEVEL_SUFFIX);
                    loggerContext.getLogger(name).setLevel(Level.toLevel(value));

                } else if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
                    val name = fetchLoggerName(key, CONSOLE_LEVEL_SUFFIX);
                    effectorContext.getEffector(name)
                            .setConsoleLevel(Level.toLevel(value));

                } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
                    val name = fetchLoggerName(key, CONSOLE_CHARSET_SUFFIX);
                    val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
                    consoleAppender.setCharset(value);
                    appenderList.addIfAbsent(consoleAppender);

                } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
                    val name = fetchLoggerName(key, CONSOLE_PATTERN_SUFFIX);
                    val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
                    consoleAppender.setPattern(value);
                    appenderList.addIfAbsent(consoleAppender);

                } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
                    val name = fetchLoggerName(key, CONSOLE_TARGET_SUFFIX);
                    val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
                    consoleAppender.setTarget(value);
                    appenderList.addIfAbsent(consoleAppender);

                } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
                    val name = fetchLoggerName(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX);
                    val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
                    consoleAppender.setImmediateFlush(toBool(value));
                    appenderList.addIfAbsent(consoleAppender);

                }
            }

            for (val appender : appenderList) {
                appender.start();
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

    private boolean toBool(String str) {
        for (val any : TRUE_VALUES) {
            if (equalsIgnoreCase(str, any)) return true;
        }
        return false;
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

    private String fetchLoggerName(String key, String suffix) {
        return key.substring(0, key.length() - suffix.length());
    }

    private ConsoleAppender fetchConsoleAppender(Logger logger) {
        val consoleAppenderName = "ConsoleAppender-" + logger.getName();
        var consoleAppender = logger.getAppender(consoleAppenderName);
        if (!(consoleAppender instanceof ConsoleAppender)) {
            logger.detachAppender(consoleAppender);
            consoleAppender = new ConsoleAppender(effectorContext);
            consoleAppender.setName(consoleAppenderName);
            consoleAppender.setContext(loggerContext);
            logger.addAppender(consoleAppender);
        }
        return (ConsoleAppender) consoleAppender;
    }
}
