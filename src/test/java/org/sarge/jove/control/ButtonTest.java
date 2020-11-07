package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.control.Button.Modifier;
import org.sarge.jove.control.Button.Operation;
import org.sarge.jove.control.InputEvent.Type.Parser;

public class ButtonTest {
	private static final String ID = "KEY";
	private static final String NAME = "KEY-PRESS-CONTROL";

	private Button button;

	@BeforeEach
	void before() {
		button = new Button(ID, Operation.PRESS.ordinal(), IntegerEnumeration.mask(Modifier.CONTROL));
	}

	@Test
	void constructor() {
		assertEquals(ID, button.id());
		assertEquals(Operation.PRESS, button.operation());
		assertEquals(Set.of(Modifier.CONTROL), button.modifiers());
		assertEquals(NAME, button.name());
	}

	@Test
	void of() {
		button = Button.of(ID);
		assertEquals(ID, button.id());
		assertEquals(Operation.PRESS, button.operation());
		assertEquals(Set.of(), button.modifiers());
		assertEquals("KEY-PRESS", button.name());
	}

	@Test
	void parse() {
		assertEquals(button, Button.parse(NAME));
	}

	@Test
	void parser() {
		final Parser parser = new Parser();
		assertEquals(button, parser.parse(Button.class.getName() + " " + NAME));
	}

	@Test
	void equals() {
		assertEquals(true, button.equals(button));
		assertEquals(true, button.equals(new Button(ID, Operation.PRESS.ordinal(), IntegerEnumeration.mask(Modifier.CONTROL))));
		assertEquals(false, button.equals(null));
		assertEquals(false, button.equals(Button.of(ID)));
	}

	@Nested
	class BuilderTests {
		private Button.Builder builder;

		@BeforeEach
		void before() {
			builder = new Button.Builder();
		}

		@Test
		void build() {
			final Button result = builder
					.id(ID)
					.operation(Operation.PRESS)
					.modifier(Modifier.CONTROL)
					.build();
			assertEquals(button, result);
		}

		@Test
		void buildRequiresIdentifier() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
