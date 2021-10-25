package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

public class AxisEventTest {
	private AxisEvent axis;
	private Type type;
	private Source src;

	@BeforeEach
	void before() {
		type = new Type("axis");
		src = mock(Source.class);
		axis = new AxisEvent(type, src, 3f);
	}

	@Test
	void constructor() {
		assertEquals("axis", axis.name());
		assertEquals(type, axis.type());
		assertEquals(src, axis.source());
		assertEquals(3f, axis.value());
	}
}
