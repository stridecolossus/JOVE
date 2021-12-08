package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.ModifiedButton.Modifier;
import org.sarge.jove.util.IntegerEnumeration;

public class ModifiedButtonTest {
	private static final Set<Modifier> MODS = Set.of(Modifier.CONTROL, Modifier.SHIFT);

	private ModifiedButton button;

	@BeforeEach
	void before() {
		button = new ModifiedButton("button", Action.PRESS, IntegerEnumeration.mask(MODS));
	}

	@Test
	void constructor() {
		assertEquals(Action.PRESS, button.action());
		assertEquals(MODS, button.modifiers());
		assertEquals("button-PRESS-SHIFT-CONTROL", button.name());
	}

	@Test
	void resolve() {
		button = button.resolve(0, 0);
		assertEquals(Action.RELEASE, button.action());
		assertEquals(Set.of(), button.modifiers());
		assertEquals("button-RELEASE", button.name());
	}
}
