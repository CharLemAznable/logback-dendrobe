package com.github.charlemaznable.logback.dendrobe.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.val;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Getter
final class ConsoleTarget {

    @AllArgsConstructor
    static class SystemStream extends OutputStream {

        @Delegate(excludes = Closeable.class)
        private OutputStream stream;
    }

    static final ConsoleTarget SYSTEM_OUT
            = new ConsoleTarget("System.out", new SystemStream(System.out));

    static final ConsoleTarget SYSTEM_ERR
            = new ConsoleTarget("System.err", new SystemStream(System.err));

    static final ConsoleTarget[] values = new ConsoleTarget[]{SYSTEM_OUT, SYSTEM_ERR};

    static ConcurrentHashMap<String, MockBuffer> mocks = new ConcurrentHashMap<>();
    static volatile boolean testMode = false;

    private final String name;
    private final OutputStream stream;

    static ConsoleTarget findByName(String name) {
        for (val target : ConsoleTarget.values()) {
            if (target.name.equalsIgnoreCase(name)) {
                return target;
            }
        }
        if (!testMode) return null;
        return new ConsoleTarget(name, mockBufferByName(name));
    }

    static ConsoleTarget[] values() {
        return values;
    }

    static void setUpMockConsole() {
        testMode = true;
    }

    static void tearDownMockConsole() {
        mocks.clear();
        testMode = false;
    }

    static MockBuffer mockBufferByName(String name) {
        if (!testMode) return null;
        return mocks.computeIfAbsent(name, k -> new MockBuffer());
    }

    @Override
    public String toString() {
        return name;
    }

    static class MockBuffer extends OutputStream {

        private String output;
        private StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(int b) {
            stringBuilder.append((byte) b);
        }

        @Override
        public void write(@Nonnull byte[] b) {
            write(b, 0, b.length);
        }

        @Override
        public void write(@Nonnull byte[] b, int off, int len) {
            stringBuilder.append(new String(b, off, len));
        }

        @Override
        public void flush() {
            output = stringBuilder.toString();
        }

        public String output() {
            return output;
        }
    }
}
