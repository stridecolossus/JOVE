package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.AbstractEvent;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

public class EventTest {
	@Test
	void type() {
		final Type type = new Type("type");
		assertEquals("type", type.name());
	}

	@Test
	void name() {
		assertEquals("one-two", Event.name("one", "two"));
	}

	@Nested
	class AbstractEventTest {
		private Event event;
		private Type type;
		private Source src;

		@BeforeEach
		void before() {
			type = new Type("type");
			src = mock(Source.class);
			event = new AbstractEvent("name", type, src) {
				// Empty
			};
		}

		@Test
		void constructor() {
			assertEquals("name", event.name());
			assertEquals(type, event.type());
			assertEquals(src, event.source());
		}
	}
}
