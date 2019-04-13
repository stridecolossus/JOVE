package org.sarge.jove.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KeyNameTest {
	@Test
	public void name() {
		assertEquals("A", KeyName.name(65));
		assertEquals("Shift", KeyName.name(16));
		assertEquals(null, KeyName.name(0));
		assertEquals(null, KeyName.name(999));
	}

	@Test
	public void reverse() {
		final var reverse = KeyName.reverse();
		assertNotNull(reverse);
		assertEquals(Integer.valueOf(65), reverse.get("A"));
		assertEquals(Integer.valueOf(16), reverse.get("Shift"));
		assertEquals(null, reverse.get("cobblers"));
	}
}
