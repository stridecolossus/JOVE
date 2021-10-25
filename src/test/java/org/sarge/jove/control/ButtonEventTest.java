package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

public class ButtonEventTest {
	private ButtonEvent button;
	private Type type;
	private Source src;

	@BeforeEach
	void before() {
		type = new Type("type");
		src = mock(Source.class);
		button = new ButtonEvent("name", type, src);
	}

	@Test
	void constructor() {
		assertEquals("name", button.name());
		assertEquals(type, button.type());
		assertEquals(src, button.source());
	}
}
