package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event;

public class JoystickAxisTest {
	private JoystickAxis axis;

	@BeforeEach
	void before() {
		axis = new JoystickAxis(1, 2);
	}

	@Test
	void constructor() {
		assertEquals(2, axis.value());
	}

	@Test
	void update() {
		axis.update(3);
		assertEquals(3, axis.value());
	}

	@Test
	void events() {
		final Consumer<Event> handler = mock(Consumer.class);
		axis.bind(handler);
		axis.update(3);
		verify(handler).accept(new AxisEvent(axis, 3));
		axis.update(3);
		verifyNoMoreInteractions(handler);
	}
}
