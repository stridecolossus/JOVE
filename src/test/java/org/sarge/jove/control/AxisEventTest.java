package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;

public class AxisEventTest {
	private Axis axis;
	private AxisEvent event;

	@BeforeEach
	void before() {
		axis = mock(Axis.class);
		event = new AxisEvent(axis, 2);
	}

	@Test
	void constructor() {
		assertEquals(axis, event.type());
		assertEquals(2, event.value());
	}

	@Test
	void equals() {
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(new AxisEvent(axis, 2)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(new AxisEvent(axis, 3)));
	}
}
