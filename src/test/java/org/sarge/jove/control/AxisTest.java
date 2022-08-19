package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

class AxisTest {
	private Axis axis;

	@BeforeEach
	void before() {
		axis = spy(Axis.class);
	}

	@Test
	void constructor() {
		assertEquals(0, axis.value());
	}

	@Test
	void value() {
		axis.update(42);
		assertEquals(42, axis.value());
	}

	@Test
	void matches() {
		assertEquals(true, axis.matches(axis));
		assertEquals(false, axis.matches(mock(Axis.class)));
	}

	@Test
	void equals() {
		assertEquals(axis, axis);
		assertNotEquals(axis, null);
		assertNotEquals(axis, mock(Axis.class));
	}
}
