package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Axis.AxisHandler;
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

	@Test
	void handler() {
		final AxisHandler consumer = mock(AxisHandler.class);
		final Consumer<AxisEvent> handler = axis.handler(consumer);
		assertNotNull(handler);
		handler.accept(axis.new AxisEvent(3));
		verify(consumer).handle(3f);
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
