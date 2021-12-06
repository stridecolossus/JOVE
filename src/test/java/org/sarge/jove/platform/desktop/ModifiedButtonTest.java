package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.desktop.DesktopButton.Action;
import org.sarge.jove.platform.desktop.ModifiedButton.Modifier;
import org.sarge.jove.util.IntegerEnumeration;

public class ModifiedButtonTest {
	private static final Set<Modifier> MODS = Set.of(Modifier.CONTROL, Modifier.SHIFT);

	private ModifiedButton button;

	@BeforeEach
	void before() {
		button = new ModifiedButton("name");
	}

	@Test
	void constructor() {
		assertEquals("name-RELEASE", button.name());
		assertEquals(Action.RELEASE, button.action());
		assertEquals(Set.of(), button.modifiers());
		assertEquals(button, button.type());
	}

	@Test
	void resolve() {
		final ModifiedButton resolved = button.resolve(1, IntegerEnumeration.mask(MODS));
		assertEquals("name-PRESS-SHIFT-CONTROL", resolved.name());
		assertEquals(Action.PRESS, resolved.action());
		assertEquals(MODS, resolved.modifiers());
	}

	@Nested
	class ModifierTests {
		@Test
		void map() {
			final var mapping = IntegerEnumeration.mapping(Modifier.class);
			assertEquals(Modifier.SHIFT, mapping.map(0x0001));
			assertEquals(Modifier.CONTROL, mapping.map(0x0002));
			assertEquals(Modifier.ALT, mapping.map(0x0004));
			assertEquals(Modifier.SUPER, mapping.map(0x0008));
			assertEquals(Modifier.CAPS_LOCK, mapping.map(0x0010));
			assertEquals(Modifier.NUM_LOCK, mapping.map(0x0020));
		}

		@Test
		void mask() {
			assertEquals(Set.of(Modifier.SHIFT, Modifier.CONTROL, Modifier.ALT), IntegerEnumeration.mapping(Modifier.class).enumerate(0x0001 | 0x0002 | 0x0004));
		}
	}
}