package com.github.charlemaznable.logback.dendrobe.appender;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThrowableProxyElfTest {

    @Test
    public void testThrowableProxyElf() {
        val error = ThrowableProxyElf.toString(new NullPointerException());
        assertTrue(error.startsWith("java.lang.NullPointerException: null\n" +
                "\tat com.github.charlemaznable.logback.dendrobe.appender.ThrowableProxyElfTest." +
                "testThrowableProxyElf(ThrowableProxyElfTest.java:12)"));
    }
}
