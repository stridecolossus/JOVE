package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.ModifiedButton.Modifier;
import org.sarge.jove.util.IntegerEnumeration;

public class ModifiedButtonTest {
	private static final Set<Modifier> MODS = Set.of(Modifier.CONTROL, Modifier.SHIFT);
	private static final int MASK = IntegerEnumeration.mask(MODS);

	private ModifiedButton button;

	@BeforeEach
	void before() {
		button = new ModifiedButton("button", Action.PRESS, MASK);
	}

	@Test
	void constructor() {
		assertEquals(Action.PRESS, button.action());
		assertEquals(MODS, button.modifiers());
		assertEquals("button-PRESS-SHIFT-CONTROL", button.name());
	}

	@Test
	void resolve() {
		final ModifiedButton resolved = button.resolve(0);
		assertNotNull(resolved);
		assertEquals(Action.RELEASE, resolved.action());
		assertEquals(MODS, resolved.modifiers());
		assertEquals("button-RELEASE-SHIFT-CONTROL", resolved.name());
	}

	@Test
	void resolveModifiers() {
		final ModifiedButton resolved = button.resolve(1, Modifier.ALT.value());
		assertNotNull(resolved);
		assertEquals(Action.PRESS, resolved.action());
		assertEquals(Set.of(Modifier.ALT), resolved.modifiers());
		assertEquals("button-PRESS-ALT", resolved.name());
	}

	@Test
	void matches() {
		assertEquals(true, button.matches(button));
		assertEquals(false, new ModifiedButton("button").matches(button));
		assertEquals(false, new ModifiedButton("other").matches(button));
	}

	@Test
	void matchesAction() {
		assertEquals(true, new DefaultButton("button", Action.PRESS).matches(button));
		assertEquals(false, new DefaultButton("button", Action.RELEASE).matches(button));
		assertEquals(false, new DefaultButton("button", Action.REPEAT).matches(button));
	}

	@Test
	void matchesModifiers() {
		assertEquals(true, new ModifiedButton("button", null, MASK).matches(button));
		assertEquals(false, new ModifiedButton("button", null, 0).matches(button));
	}
	@Test
	void matchesModifierMasked() {
		assertEquals(true, new ModifiedButton("button", null, MASK | Modifier.ALT.value()).matches(button));
		assertEquals(false, new ModifiedButton("button", null, Modifier.SHIFT.value() | Modifier.ALT.value()).matches(button));
	}

	@Test
	void matchesActionModifiers() {
		assertEquals(true, new ModifiedButton("button", Action.PRESS, MASK).matches(button));
		assertEquals(false, new ModifiedButton("button", Action.PRESS, 0).matches(button));
		assertEquals(false, new ModifiedButton("button", Action.RELEASE, MASK).matches(button));
	}
}
