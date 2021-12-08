package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton.Action;

public class DefaultButtonTest {
	private static final String BUTTON = "button";

	private DefaultButton button;

	@BeforeEach
	void before() {
		button = new DefaultButton(BUTTON);
	}

	@Test
	void constructor() {
		assertEquals(BUTTON, button.id());
		assertEquals(Action.PRESS, button.action());
		assertEquals("button-PRESS", button.name());
	}

	@Test
	void invalidAction() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Action.map(999));
	}

	@Test
	void matches() {
		assertEquals(true, button.matches(button));
		assertEquals(true, button.matches(new DefaultButton(BUTTON)));
		assertEquals(false, button.matches(new DefaultButton("other")));
	}

	@Test
	void matchesAction() {
		assertEquals(true, new DefaultButton(BUTTON, null).matches(button));
		assertEquals(true, new DefaultButton(BUTTON, Action.PRESS).matches(button));
		assertEquals(false, new DefaultButton(BUTTON, Action.RELEASE).matches(button));
		assertEquals(false, new DefaultButton(BUTTON, Action.REPEAT).matches(button));
	}

	@Test
	void resolve() {
		final Button resolved = button.resolve(0);
		assertNotNull(resolved);
		assertEquals(Action.RELEASE, resolved.action());
		assertEquals("button-RELEASE", resolved.name());
	}

	@Test
	void resolveSelf() {
		assertEquals(button, button.resolve(1));
	}
}
