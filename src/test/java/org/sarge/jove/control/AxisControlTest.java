package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

class AxisControlTest {
	private AxisControl axis;

	@BeforeEach
	void before() {
		axis = spy(AxisControl.class);
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
	void equals() {
		assertEquals(axis, axis);
		assertNotEquals(axis, null);
		assertNotEquals(axis, mock(AxisControl.class));
	}
}
