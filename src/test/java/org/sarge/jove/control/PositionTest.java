package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Position.PositionEvent;

public class PositionTest {
	private Position pos;
	private Source src;

	@BeforeEach
	void before() {
		src = mock(Source.class);
		pos = new Position("pos", src);
	}

	@Test
	void constructor() {
		assertEquals("pos", pos.name());
		assertEquals(src, pos.source());
	}

	@Nested
	class PositionEventTests {
		private PositionEvent event;

		@BeforeEach
		void before() {
			event = pos.new PositionEvent(1, 2);
		}

		@Test
		void constructor() {
			assertEquals(1, event.x);
			assertEquals(2, event.y);
			assertEquals(pos, event.type());
		}
	}
}
