package com.github.charlemaznable.logback.miner.configurator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.auto.service.AutoService;
import lombok.val;

import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchConsoleAppender;
import static com.github.charlemaznable.logback.miner.configurator.ConfiguratorUtil.fetchLoggerName;
import static com.github.charlemaznable.logback.miner.level.EffectorContextUtil.getEffectorContext;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.toBool;

@AutoService(Configurator.class)
public class ConsoleConfigurator extends AppenderConfigurator {

    private static final String CONSOLE_LEVEL_SUFFIX = "[console.level]";
    private static final String CONSOLE_CHARSET_SUFFIX = "[console.charset]";
    private static final String CONSOLE_PATTERN_SUFFIX = "[console.pattern]";
    private static final String CONSOLE_TARGET_SUFFIX = "[console.target]";
    private static final String CONSOLE_IMMEDIATE_FLUSH_SUFFIX = "[console.immediateFlush]";

    @Override
    public void configurate(LoggerContext loggerContext, String key, String value) {
        if (endsWithIgnoreCase(key, CONSOLE_LEVEL_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_LEVEL_SUFFIX);
            val effectorContext = getEffectorContext(loggerContext);
            if (isNull(effectorContext)) return;
            effectorContext.getEffector(name).setConsoleLevel(Level.toLevel(value));
            addAppenderIfAbsent(fetchConsoleAppender(loggerContext.getLogger(name)));

        } else if (endsWithIgnoreCase(key, CONSOLE_CHARSET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_CHARSET_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setCharset(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_PATTERN_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_PATTERN_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setPattern(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_TARGET_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_TARGET_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setTarget(value);
            addAppenderIfAbsent(consoleAppender);

        } else if (endsWithIgnoreCase(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX)) {
            val name = fetchLoggerName(key, CONSOLE_IMMEDIATE_FLUSH_SUFFIX);
            val consoleAppender = fetchConsoleAppender(loggerContext.getLogger(name));
            consoleAppender.setImmediateFlush(toBool(value));
            addAppenderIfAbsent(consoleAppender);

        }
    }
}
