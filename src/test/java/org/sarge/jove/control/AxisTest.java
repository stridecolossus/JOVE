package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.Source;

public class AxisTest {
	private Axis axis;
	private Source src;

	@BeforeEach
	void before() {
		src = mock(Source.class);
		axis = new Axis("axis", src);
	}

	@Test
	void constructor() {
		assertEquals("axis", axis.name());
		assertEquals(src, axis.source());
	}

	@Nested
	class AxisEventTests {
		private AxisEvent event;

		@BeforeEach
		void before() {
			event = axis.new AxisEvent(3);
		}

		@Test
		void constructor() {
			assertEquals(3, event.value());
			assertEquals(axis, event.type());
		}
	}
}
