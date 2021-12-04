package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;

public class PositionEventTest {
	private Source src;
	private PositionEvent event;

	@BeforeEach
	void before() {
		src = mock(Source.class);
		event = new PositionEvent(src, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(src, event.type());
		assertEquals(2, event.x());
		assertEquals(3, event.y());
	}

	@Test
	void equals() {
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(new PositionEvent(src, 2, 3)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(new PositionEvent(src, 3, 4)));
	}
}
