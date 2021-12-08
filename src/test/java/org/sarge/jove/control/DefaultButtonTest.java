package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton.Action;

public class DefaultButtonTest {
	private DefaultButton button;

	@BeforeEach
	void before() {
		button = new DefaultButton("button");
	}

	@Test
	void constructor() {
		assertEquals(Action.RELEASE, button.action());
		assertEquals("button-RELEASE", button.name());
	}

	@Test
	void invalidAction() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Action.map(999));
	}

	@Test
	void resolve() {
		button = button.resolve(1, 0);
		assertEquals(Action.PRESS, button.action());
		assertEquals("button-PRESS", button.name());
	}

	@Test
	void resolveInvalidModifiers() {
		assertThrows(IllegalArgumentException.class, () -> button.resolve(1, 2));
	}
}
