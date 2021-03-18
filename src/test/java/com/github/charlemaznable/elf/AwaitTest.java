package com.github.charlemaznable.elf;

import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AwaitTest {

    @Test
    public void testAwait() {
        assertThrows(ReflectException.class, () ->
                onClass(Await.class).create().get());
    }
}
