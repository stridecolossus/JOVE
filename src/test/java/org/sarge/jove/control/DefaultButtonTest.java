package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.DefaultButton.Modifier;
import org.sarge.jove.util.IntegerEnumeration;

public class DefaultButtonTest {
	private static final Set<Modifier> MODS = Set.of(Modifier.CONTROL, Modifier.SHIFT);

	private DefaultButton button;

	@BeforeEach
	void before() {
		button = new DefaultButton("button", Action.PRESS, IntegerEnumeration.mask(MODS));
	}

	@Test
	void constructor() {
		assertEquals(Action.PRESS, button.action());
		assertEquals(MODS, button.modifiers());
		assertEquals("button-PRESS-SHIFT-CONTROL", button.name());
	}

	@Test
	void constructorUnmodified() {
		button = new DefaultButton("button");
		assertEquals(Action.PRESS, button.action());
		assertEquals(Set.of(), button.modifiers());
		assertEquals("button-PRESS", button.name());
	}

	@Test
	void invalidAction() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Action.map(999));
	}

	@Test
	void resolve() {
		button = button.resolve(0, 0);
		assertEquals(Action.RELEASE, button.action());
		assertEquals(Set.of(), button.modifiers());
		assertEquals("button-RELEASE", button.name());
	}
}
