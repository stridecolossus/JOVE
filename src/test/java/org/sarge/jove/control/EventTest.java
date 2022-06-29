package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EventTest {
	@SuppressWarnings("static-method")
	@Test
	void name() {
		assertEquals("one-2", Event.name("one", 2));
	}
}
