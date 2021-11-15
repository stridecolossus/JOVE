package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Button.Modifier;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.util.IntegerEnumeration;

public class ButtonTest {
	private Button button;
	private Source src;

	@BeforeEach
	void before() {
		src = mock(Source.class);
		button = new Button("name", src);
	}

	@Test
	void constructor() {
		assertEquals("name-PRESS", button.name());
		assertEquals(Action.PRESS, button.action());
		assertEquals(0, button.mods());
		assertEquals(Set.of(), button.modifiers());
		assertEquals(src, button.source());
		assertEquals(button, button.type());
	}

	@Test
	void resolve() {
		final int mods = 0x0001 | 0x0002;
		final Button result = button.resolve(Action.RELEASE, mods);
		assertNotNull(result);
		assertEquals("name-RELEASE-SHIFT-CONTROL", result.name());
		assertEquals(Action.RELEASE, result.action());
		assertEquals(mods, result.mods());
		assertEquals(Set.of(Modifier.SHIFT, Modifier.CONTROL), result.modifiers());
		assertEquals(src, result.source());
		assertEquals(result, result.type());
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
