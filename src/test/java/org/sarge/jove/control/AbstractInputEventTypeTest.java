package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.AbstractInputEventType;
import org.sarge.jove.control.InputEvent.Type;

public class AbstractInputEventTypeTest {
	private Type type;

	@BeforeEach
	void before() {
		type = create("Name", 42);
	}

	private Type create(Object... args) {
		return new AbstractInputEventType(args) {
			@Override
			public boolean equals(Object obj) {
				return obj == type;
			}

			@Override
			public Type parse(String[] tokens) {
				return type;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals("Name-42", type.name());
	}

	@Test
	void constructorEmptyArgument() {
		assertThrows(IllegalArgumentException.class, () -> create(""));
	}

	@Test
	void constructorArgumentContainsSpace() {
		assertThrows(IllegalArgumentException.class, () -> create("a space"));
	}

	@Test
	void constructorArgumentContainsDelimiter() {
		assertThrows(IllegalArgumentException.class, () -> create("a-slash"));
	}

	@Test
	void parse() {
		assertEquals(type, AbstractInputEventType.parse("Name-42"));
	}

	@Test
	void parseUnknown() {
		assertThrows(UnsupportedOperationException.class, () -> AbstractInputEventType.parse("cobblers"));
	}

	@Test
	void hash() {
		assertEquals(type.hashCode(), type.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, type.equals(type));
		assertEquals(false, type.equals(null));
		assertEquals(false, type.equals(mock(Type.class)));
	}
}
