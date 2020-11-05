package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Device;
import org.sarge.jove.control.InputEvent.Source;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Parser;

public class InputEventTest {
	private static final String CLASSNAME = "org.sarge.jove.control.InputEventTest$MockEventType";
	private static final String NAME = "name";

	@Test
	void enable() {
		// Create a device
		final Device dev = mock(Device.class);
		final Source<?> src = mock(Source.class);
		when(dev.sources()).thenReturn(Set.of(src));

		// Enable all sources
		@SuppressWarnings("unchecked")
		final Consumer<InputEvent<?>> handler = mock(Consumer.class);
		Device.enable(dev, handler);
		verify(src).enable(handler);
	}

	@Nested
	class ParserTests {
		private Parser parser;

		@BeforeEach
		void before() {
			parser = new Parser();
		}

		@Test
		void write() {
			assertEquals(CLASSNAME + Type.DELIMITER + NAME, Type.write(new MockEventType(NAME)));
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

		@Test
		void parseInvalidType() {
			assertThrows(IllegalArgumentException.class, "Not an event class", () -> parser.parse("java.lang.String-whatever"));
		}

		@Test
		void parseMissingParseMethod() {
			assertThrows(IllegalArgumentException.class, "Cannot find event constructor", () -> parser.parse("org.sarge.jove.control.InputEventTest$InvalidEventType-whatever"));
		}
	}

	static class MockEventType implements Type {
		private MockEventType(String name) {
			assertEquals(NAME, name);
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
