package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.control.Button.ButtonAction.*;
import static org.sarge.jove.control.Button.ModifierKey.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button.*;

class ButtonTest {
	@Nested
	class ActionTest {
		@Test
		void map() {
			assertEquals(RELEASE, ButtonAction.map(0));
			assertEquals(PRESS, ButtonAction.map(1));
			assertEquals(REPEAT, ButtonAction.map(2));
		}

		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> ButtonAction.map(-1));
		}
	}

	@Test
	void modifiers() {
		assertEquals(Set.of(SHIFT, CONTROL, ALT, SUPER), ModifierKey.map(0b01111));
	}
}
