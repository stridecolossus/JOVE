package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button.Action;

public class ButtonTest {
	private Button<Action> button;

	@BeforeEach
	void before() {
		button = new Button<>("button", Action.PRESS);
		// TODO - modifiers
	}

	@Test
	void constructor() {
		assertEquals("button", button.id());
		assertEquals(Action.PRESS, button.action());
//		assertEquals(2, button.modifiers());
	}

	@Test
	void equals() {
		assertEquals(button, button);
		assertEquals(button, new Button<>("button", Action.PRESS));
		assertNotEquals(button, null);
		assertNotEquals(button, new Button<>("button", Action.RELEASE));
	}
}
