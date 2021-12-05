package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.desktop.DesktopButton.Action;

public class DesktopButtonTest {
	private DesktopButton button;

	@BeforeEach
	void before() {
		button = new DesktopButton("name");
	}

	@Test
	void constructor() {
		assertEquals("name-RELEASE", button.name());
		assertEquals(Action.RELEASE, button.action());
		assertEquals(button, button.type());
	}

	@Test
	void resolve() {
		final DesktopButton pressed = button.resolve(Action.PRESS);
		assertNotNull(pressed);
		assertEquals(Action.PRESS, pressed.action());
		assertEquals(button, button.resolve(Action.RELEASE));
	}

	@Nested
	class ActionTests {
		@Test
		void map() {
			assertEquals(Action.RELEASE, Action.map(0));
			assertEquals(Action.PRESS, Action.map(1));
			assertEquals(Action.REPEAT, Action.map(2));
		}

		@Test
		void invalid() {
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> Action.map(999));
		}
	}
}
