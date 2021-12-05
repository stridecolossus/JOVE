package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button.AbstractButton;

public class ButtonTest {
	@Test
	void name() {
		assertEquals("one-2", Button.name("one", 2));
	}

	@Nested
	class AbstractButtonTests {
		private Button button;

		@BeforeEach
		void before() {
			button = spy(AbstractButton.class);
		}

		@Test
		void constructor() {
			assertEquals(button, button.type());
		}

		@Test
		void equals() {
			assertEquals(true, button.equals(button));
			assertEquals(false, button.equals(null));
			assertEquals(false, button.equals(mock(Button.class)));
		}
	}
}
