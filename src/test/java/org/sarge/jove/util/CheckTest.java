package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CheckTest {
    @Test
    void isNullObject() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notNull(null));
    }

    @Test
    void notNullObject() {
        final Object obj = new Object();
        assertEquals(obj, Check.notNull(obj));
    }

    @Test
    void isEmptyString() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(new String("")));
    }

    @Test
    void notEmptyString() {





        final String str = "string";
        assertEquals(str, Check.notEmpty(str));
    }

    @Test
    void emptyCollection() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(List.of()));
    }

    @Test
    void notEmptyCollection() {
    	final var list = List.of(new Object());
        assertEquals(list, Check.notEmpty(list));
    }

    @Test
    void emptyMap() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(Map.of()));
    }

    @Test
    void notEmptyMap() {
    	Check.notEmpty(Collections.singletonMap("key", "value"));
    }

    @Test
    void emptyArray() {
    	assertThrows(IllegalArgumentException.class, () -> Check.notEmpty(new Object[]{}));
    }

    @Test
    void notEmptyArray() {
        final String[] array = { "string" };
        assertArrayEquals(array, Check.notEmpty(array));
    }

    @Test
    void zeroOrMore() {
        Check.zeroOrMore(0);
        Check.zeroOrMore(1);
    	assertThrows(IllegalArgumentException.class, () -> Check.zeroOrMore(-1));
    }

    @Test
    void oneOrMore() {
        assertEquals(Integer.valueOf(1), Check.oneOrMore(1));
        Check.oneOrMore(1f);
        Check.oneOrMore(1L);
    	assertThrows(IllegalArgumentException.class, () -> Check.oneOrMore(0));
    	assertThrows(IllegalArgumentException.class, () -> Check.oneOrMore(-1));
    	assertThrows(IllegalArgumentException.class, () -> Check.oneOrMore(0.5f));
    }

    @Test
    void positive() {
    	Check.positive(1);
    	Check.positive(1f);
    	Check.positive(1L);
    	assertThrows(IllegalArgumentException.class, () -> Check.positive(0));
    	assertThrows(IllegalArgumentException.class, () -> Check.positive(-1));
    }

    @Test
    void range() {
        Check.range(0, 0, 1);
        Check.range(1, 0, 1);
    	assertThrows(IllegalArgumentException.class, () -> Check.range(-1, 0, 1));
    	assertThrows(IllegalArgumentException.class, () -> Check.range(2, 0, 1));
    }

    @Test
    void percentile() {
    	Check.isPercentile(0);
    	Check.isPercentile(1);
    	assertThrows(IllegalArgumentException.class, () -> Check.isPercentile(-1));
    	assertThrows(IllegalArgumentException.class, () -> Check.isPercentile(2));
    }
}
