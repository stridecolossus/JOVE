package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event.Source;

public class ButtonTest {
	private Button<Action> button;
	private Source<Button<Action>> source;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		source = mock(Source.class);
		button = new Button<>(source, "button", Action.PRESS);
		// TODO - modifiers
	}

	@Test
	void constructor() {
		assertEquals(source, button.source());
		assertEquals("button", button.id());
		assertEquals(Action.PRESS, button.action());
//		assertEquals(2, button.modifiers());
	}

	@Test
	void equals() {
		assertEquals(button, button);
		assertEquals(button, new Button<>(source, "button", Action.PRESS));
		assertNotEquals(button, null);
		assertNotEquals(button, new Button<>(source, "button", Action.RELEASE));
	}
}
