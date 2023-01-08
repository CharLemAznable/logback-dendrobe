package com.github.charlemaznable.logback.dendrobe.cloud;

import ch.qos.logback.classic.LoggerContext;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.gentle.spring.factory.SpringFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.cloud.bootstrap.LoggingSystemShutdownListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.security.CodeSource;
import java.security.ProtectionDomain;

@SpringFactory(ApplicationListener.class)
public final class LogbackDendrobeReinitialListener
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    public static final int DEFAULT_ORDER = LoggingSystemShutdownListener.DEFAULT_ORDER + 1;

    @Getter
    @Setter
    private int order = DEFAULT_ORDER;

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEnvironmentPreparedEvent event) {
        if (ClzPath.classExists("org.springframework.cloud.bootstrap.LoggingSystemShutdownListener")) {
            getLoggerContext().putObject(LoggingSystem.class.getName(), new Object());
        }
    }

    private LoggerContext getLoggerContext() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        Assert.isInstanceOf(LoggerContext.class, factory, () -> String.format(
                "LoggerFactory is not a Logback LoggerContext but Logback is on "
                        + "the classpath. Either remove Logback or the competing "
                        + "implementation (%s loaded from %s). If you are using "
                        + "WebLogic you will need to add 'org.slf4j' to "
                        + "prefer-application-packages in WEB-INF/weblogic.xml",
                factory.getClass(), getLocation(factory)));
        return (LoggerContext) factory;
    }

    private Object getLocation(ILoggerFactory factory) {
        try {
            ProtectionDomain protectionDomain = factory.getClass().getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                return codeSource.getLocation();
            }
        } catch (SecurityException ex) {
            // Unable to determine location
        }
        return "unknown location";
    }
}
