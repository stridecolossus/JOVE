package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

	@DisplayName("Resolved button should retain the modifiers")
	@Test
	void resolve() {
		final ModifiedButton resolved = button.resolve(0);
		assertNotNull(resolved);
		assertEquals(Action.RELEASE, resolved.action());
		assertEquals(MODS, resolved.modifiers());
		assertEquals("button-RELEASE-SHIFT-CONTROL", resolved.name());
	}

	@DisplayName("Resolved modifiers should replace the modifiers")
	@Test
	void resolveModifiers() {
		final ModifiedButton resolved = button.resolve(1, Modifier.ALT.value());
		assertNotNull(resolved);
		assertEquals(Action.PRESS, resolved.action());
		assertEquals(Set.of(Modifier.ALT), resolved.modifiers());
		assertEquals("button-PRESS-ALT", resolved.name());
	}

	@DisplayName("Modified button template should match buttons with the same id, action and modifiers")
	@Test
	void matchesModifiers() {
		assertEquals(true, button.matches(button));
		assertEquals(true, button.matches(new ModifiedButton("button", Action.PRESS, MASK)));
		assertEquals(false, new ModifiedButton("button").matches(button));
		assertEquals(false, new ModifiedButton("other", Action.PRESS, MASK).matches(button));
		assertEquals(false, new ModifiedButton("button", Action.PRESS, 0).matches(button));
		assertEquals(false, new ModifiedButton("button", Action.RELEASE, MASK).matches(button));
	}

	@DisplayName("Modified button template should match buttons with a super-set of the modifiers")
	@Test
	void matchesModifiersMasked() {
		assertEquals(true, new ModifiedButton("button", Action.PRESS, MASK | Modifier.ALT.value()).matches(button));
		assertEquals(false, new ModifiedButton("button", Action.PRESS, Modifier.SHIFT.value() | Modifier.ALT.value()).matches(button));
	}
}
