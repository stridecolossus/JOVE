package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button.AbstractButton;

public class ButtonTest {
	@Test
	void name() {
		assertEquals("one-2", Button.name("one", 2, StringUtils.EMPTY, null));
	}

	@Nested
	class AbstractButtonTests {
		private Button button;

		@BeforeEach
		void before() {
			button = new AbstractButton("id") {
				@Override
				public String name() {
					return "name";
				}

				@Override
				public Object action() {
					return null;
				}

				@Override
				public Button resolve(int action) {
					return null;
				}
			};
		}

		@Test
		void constructor() {
			assertEquals("id", button.id());
			assertEquals(button, button.type());
		}

		@Test
		void matches() {
			assertEquals(true, button.matches(button));
			assertEquals(false, button.matches(mock(AbstractButton.class)));
		}

		@Test
		void equals() {
			assertEquals(true, button.equals(button));
			assertEquals(false, button.equals(null));
			assertEquals(false, button.equals(mock(Button.class)));
		}
	}
}
