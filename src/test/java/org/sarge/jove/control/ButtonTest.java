package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.control.Button.Modifier;
import org.sarge.jove.control.Button.Operation;

public class ButtonTest {
	private static final String NAME = "KEY";

	private Button button;

	@BeforeEach
	void before() {
		button = new Button(NAME, Operation.PRESS.ordinal(), IntegerEnumeration.mask(Modifier.CONTROL, Modifier.SHIFT));
	}

	@Test
	void constructor() {
		assertEquals(NAME, button.id());
		assertEquals(Operation.PRESS, button.operation());
		assertEquals(Set.of(Modifier.CONTROL, Modifier.SHIFT), button.modifiers());
		assertEquals("KEY-CONTROL-SHIFT-PRESS", button.name());
	}

	@Test
	void release() {
		// TODO
	}

	@Test
	void parse() {
		final var parser = new InputEvent.Type.Parser();
		final var result = parser.parse("org.sarge.jove.control.Button-KEY");
		assertEquals(button, result);
	}

	@Test
	void equals() {
		assertEquals(true, button.equals(button));
		assertEquals(true, button.equals(new Button(NAME, Operation.PRESS.ordinal(), IntegerEnumeration.mask(Modifier.CONTROL, Modifier.SHIFT))));
		assertEquals(false, button.equals(null));
		assertEquals(false, button.equals(new Button(NAME)));
	}
}
