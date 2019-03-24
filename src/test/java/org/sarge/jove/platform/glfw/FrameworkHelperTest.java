package org.sarge.jove.platform.glfw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.glfw.FrameworkHelper;

public class FrameworkHelperTest {
	@Test
	public void action() {
		assertEquals(Event.Operation.RELEASE, FrameworkHelper.operation(0));
		assertEquals(Event.Operation.PRESS, FrameworkHelper.operation(1));
		assertEquals(Event.Operation.REPEAT, FrameworkHelper.operation(2));
	}

	@Test
	public void actionInvalid() {
		assertThrows(IllegalArgumentException.class, () -> FrameworkHelper.operation(999));
	}

	@Test
	public void modifiers() {
		assertEquals(Set.of(Event.Modifier.CTRL), FrameworkHelper.modifiers(0b0001));
		assertEquals(Set.of(Event.Modifier.SHIFT, Event.Modifier.SUPER), FrameworkHelper.modifiers(0b1010));
		assertEquals(EnumSet.allOf(Event.Modifier.class), FrameworkHelper.modifiers(0b1111));
	}

	@Test
	public void modifiersInvalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> FrameworkHelper.modifiers(999));
	}
}
