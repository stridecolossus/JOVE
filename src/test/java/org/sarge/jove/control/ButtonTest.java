package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Button.Modifier;
import org.sarge.jove.util.IntegerEnumeration;

public class ButtonTest {
	private static final Set<Modifier> MODS = Set.of(Modifier.CONTROL, Modifier.SHIFT);

	private Button button;

	@BeforeEach
	void before() {
		button = new Button("name", Action.RELEASE, IntegerEnumeration.mask(MODS));
	}

	@Test
	void constructor() {
		assertEquals("name-RELEASE-SHIFT-CONTROL", button.name());
		assertEquals(Action.RELEASE, button.action());
		assertEquals(MODS, button.modifiers());
		assertEquals(button, button.type());
	}

	@Test
	void name() {
		assertEquals("one-2", Button.name("one", 2));
	}

	@Test
	void resolve() {
		final Button base = new Button("name");
		assertEquals(button, base.resolve(Action.RELEASE, IntegerEnumeration.mask(MODS)));
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
