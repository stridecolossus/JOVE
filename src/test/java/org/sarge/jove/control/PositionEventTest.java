package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

public class PositionEventTest {
	private PositionEvent pos;
	private Type type;
	private Source src;

	@BeforeEach
	void before() {
		type = new Type("type");
		src = mock(Source.class);
		pos = new PositionEvent(type, src, 1, 2);
	}

	@Test
	void constructor() {
		assertEquals("type", pos.name());
		assertEquals(type, pos.type());
		assertEquals(src, pos.source());
		assertEquals(1f, pos.x);
		assertEquals(2f, pos.y);
	}
}
