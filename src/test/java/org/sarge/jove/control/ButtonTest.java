package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Event.Source;

public class ButtonTest {
	private Button button;
	private Source<Button> source;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		source = mock(Source.class);
		button = new Button(source, "button", 1, 2);
	}

	@Test
	void constructor() {
		assertEquals(source, button.source());
		assertEquals("button", button.id());
		assertEquals(1, button.action());
		assertEquals(2, button.modifiers());
	}

	@Test
	void equals() {
		assertEquals(button, button);
		assertEquals(button, new Button(source, "button", 1, 2));
		assertNotEquals(button, null);
		assertNotEquals(button, new Button(source, "other", 1, 2));
	}
}
