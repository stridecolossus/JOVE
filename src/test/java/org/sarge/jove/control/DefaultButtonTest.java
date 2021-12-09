package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

	@DisplayName("Action code should map to the enumeration")
	@Test
	void invalidAction() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Action.map(999));
	}

	@DisplayName("Button template should match buttons with the same id and action")
	@Test
	void matches() {
		assertEquals(true, button.matches(button));
		assertEquals(true, button.matches(new DefaultButton(BUTTON)));
		assertEquals(false, button.matches(new DefaultButton("other")));
	}

	@DisplayName("Resolved button should replace the action")
	@Test
	void resolve() {
		final Button resolved = button.resolve(0);
		assertNotNull(resolved);
		assertEquals(Action.RELEASE, resolved.action());
		assertEquals("button-RELEASE", resolved.name());
	}

	@DisplayName("Resolved button should replace the action is the same button")
	@Test
	void resolveSelf() {
		assertEquals(button, button.resolve(1));
	}
}
