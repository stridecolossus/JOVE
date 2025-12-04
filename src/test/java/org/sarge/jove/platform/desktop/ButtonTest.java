package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.desktop.Button.Action.*;
import static org.sarge.jove.platform.desktop.Button.ModifierKey.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.desktop.Button.*;

class ButtonTest {
	@Nested
	class ActionTest {
		@Test
		void map() {
			assertEquals(RELEASE, Action.map(0));
			assertEquals(PRESS, Action.map(1));
			assertEquals(REPEAT, Action.map(2));
		}

		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> Action.map(-1));
		}
	}

	@Test
	void modifiers() {
		assertEquals(Set.of(SHIFT, CONTROL, ALT, SUPER), ModifierKey.map(0b01111));
	}
}
