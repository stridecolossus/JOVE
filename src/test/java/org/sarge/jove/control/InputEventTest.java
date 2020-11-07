package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sarge.jove.util.TestHelper.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Parser;

public class InputEventTest {
	private static final String CLASSNAME = "org.sarge.jove.control.InputEventTest$MockEventType";
	private static final String NAME = "name";

	@Nested
	class ParserTests {
		private Parser parser;

		@BeforeEach
		void before() {
			parser = new Parser();
		}

		@Test
		void parse() {
			final Type type = parser.parse(CLASSNAME + " " + NAME);
			assertNotNull(type);
			assertEquals(MockEventType.class, type.getClass());
		}

		@Test
		void parseInvalidString() {
			assertThrows(IllegalArgumentException.class, "Invalid event-type representation", () -> parser.parse("cobblers"));
		}

		@Test
		void parseUnknownType() {
			assertThrows(IllegalArgumentException.class, "Unknown event type", () -> parser.parse("cobblers whatever"));
		}

		@Test
		void parseInvalidType() {
			assertThrows(IllegalArgumentException.class, "Not an event class", () -> parser.parse("java.lang.String whatever"));
		}

		@Test
		void parseMissingParseMethod() {
			assertThrows(IllegalArgumentException.class, "Cannot find parse method", () -> parser.parse("org.sarge.jove.control.InputEventTest$InvalidEventType whatever"));
		}
	}

	static class MockEventType implements Type {
		static MockEventType parse(String name) {
			assertEquals(NAME, name);
			return new MockEventType();
		}

		@Override
		public String name() {
			return NAME;
		}
	}

	static class InvalidEventType implements Type {
		@Override
		public String name() {
			return null;
		}
	}
}
