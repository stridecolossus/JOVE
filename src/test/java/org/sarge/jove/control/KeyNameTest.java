package org.sarge.jove.control;

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
}
