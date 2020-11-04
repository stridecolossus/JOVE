package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sarge.jove.util.TestHelper.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Parser;

public class InputEventTest {
	private static final String CLASSNAME = "org.sarge.jove.control.InputEventTest$MockEventType";
	private static final String NAME = "name";

	static class MockEventType implements Type {
		static MockEventType parse(String str) {
			assertEquals(NAME, str);
			return new MockEventType();
		}

		private MockEventType() {
		}

		@Override
		public String name() {
			return NAME;
		}
	}

	private Parser parser;

	@BeforeEach
	void before() {
		parser = new Parser();
	}

	@Test
	void write() {
		assertEquals(CLASSNAME + Type.DELIMITER + NAME, Type.write(new MockEventType()));
	}

	@Test
	void parse() {
		final Type type = parser.parse(CLASSNAME + Type.DELIMITER + NAME);
		assertNotNull(type);
		assertEquals(MockEventType.class, type.getClass());
	}

	@Test
	void parseInvalidString() {
		assertThrows(IllegalArgumentException.class, "Invalid event-type representation", () -> parser.parse("cobblers"));
	}

	@Test
	void parseUnknownType() {
		assertThrows(IllegalArgumentException.class, "Unknown event type", () -> parser.parse("cobblers-whatever"));
	}

	static class InvalidEventType implements Type {
		@Override
		public String name() {
			return null;
		}
	}

	@Test
	void parseMissingParseMethod() {
		assertThrows(IllegalArgumentException.class, "Cannot find parse method", () -> parser.parse("org.sarge.jove.control.InputEventTest$InvalidEventType-whatever"));
	}
}
