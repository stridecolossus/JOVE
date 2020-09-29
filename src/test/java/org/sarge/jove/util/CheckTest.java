package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CheckTest {
    @Test
    public void isNullObject() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notNull(null));
    }

    @Test
    public void notNullObject() {
        final Object obj = new Object();
        assertEquals(obj, Check.notNull(obj));
    }

    @Test
    public void isEmptyString() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(new String("")));
    }

    @Test
    public void notEmptyString() {
        final String str = "string";
        assertEquals(str, Check.notEmpty(str));
    }

    @Test
    public void emptyCollection() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(List.of()));
    }

    @Test
    public void notEmptyCollection() {
    	final var list = List.of(new Object());
        assertEquals(list, Check.notEmpty(list));
    }

    @Test
    public void emptyMap() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(Map.of()));
    }

    @Test
    public void notEmptyMap() {
    	Check.notEmpty(Collections.singletonMap("key", "value"));
    }

    @Test
    public void emptyArray() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(new Object[]{}));
    }

    @Test
    public void notEmptyArray() {
        final String[] array = { "string" };
        assertArrayEquals(array, Check.notEmpty(array));
    }

    @Test
    public void zeroOrMore() {
        Check.zeroOrMore(0);
        Check.zeroOrMore(1);

        Check.zeroOrMore(0f);
        Check.zeroOrMore(1f);

        Check.zeroOrMore(0L);
        Check.zeroOrMore(1L);
    }

    @Test
    public void zeroOrMoreInvalid() {
    	assertThrows(IllegalArgumentException.class, () -> Check.zeroOrMore(-1));
    }

    @Test
    public void oneOrMore() {
        Check.oneOrMore(1);
        Check.oneOrMore(1f);
        Check.oneOrMore(1L);
    }

    @Test
    public void oneOrMoreInvalid() {
    	assertThrows(IllegalArgumentException.class, () -> Check.oneOrMore(0));
    }

    @Test
    public void range() {
        Check.range(2, 1, 3);
        Check.range(2f, 1f, 3f);
        Check.range(2L, 1L, 3L);
    }

    @Test
    public void rangeInvalid() {
    	assertThrows(IllegalArgumentException.class, () -> Check.range(42, 1, 3));
    }
}
