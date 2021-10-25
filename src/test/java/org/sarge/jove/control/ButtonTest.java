package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;

public class ButtonTest {
	private Button button;
	private Source src;

	@BeforeEach
	void before() {
		src = mock(Source.class);
		button = new Button("name", src);
	}

	@Test
	void constructor() {
		assertEquals("name", button.name());
		assertEquals(src, button.source());
		assertEquals(button, button.type());
	}
}
