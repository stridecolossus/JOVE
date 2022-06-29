package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Axis;

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

	@SuppressWarnings("unchecked")
	@Test
	void events() {
		final Consumer<Axis> handler = mock(Consumer.class);
		axis.bind(handler);
		axis.update(3);
		verify(handler).accept(axis);
		axis.update(3);
		verifyNoMoreInteractions(handler);
	}
}
