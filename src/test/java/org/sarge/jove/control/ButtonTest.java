package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button.Operation;

public class ButtonTest {
	private static final String NAME = "key";

	private Button button;

	@BeforeEach
	void before() {
		button = new Button(NAME);
	}

	@Test
	void constructor() {
		assertEquals(NAME, button.name());
	}

	@Test
	void release() {
		// TODO
	}

	@Test
	void parse() {
		assertEquals(button, Button.parse(NAME));
	}

	@Test
	void equals() {
		assertEquals(true, button.equals(button));
		assertEquals(true, button.equals(new Button(NAME)));
		assertEquals(false, button.equals(null));
		assertEquals(false, button.equals(new Button("other")));
	}

	@Nested
	class EventTests {
		private Button.Event event;

		@BeforeEach
		void before() {
			event = button.event(Operation.PRESS);
		}

		@Test
		void constructor() {
			assertEquals(button, event.type());
		}

		@Test
		void equals() {
			assertEquals(true, event.equals(event));
			assertEquals(true, event.equals(button.event(Operation.PRESS)));
			assertEquals(false, event.equals(null));
			assertEquals(false, event.equals(button.event(Operation.RELEASE)));
			assertEquals(false, event.equals(new Button("other").event(Operation.PRESS)));
		}
	}
}
