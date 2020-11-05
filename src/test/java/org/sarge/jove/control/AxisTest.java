package org.sarge.jove.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AxisTest {
	private Axis axis;

	@BeforeEach
	void before() {
		axis = new Axis("Axis");
	}

	@Test
	void constructor() {
		assertEquals("Axis", axis.name());
	}

	@Test
	void parse() {
		final var parser = new InputEvent.Type.Parser();
		final var result = parser.parse("org.sarge.jove.control.Axis-Axis");
		assertEquals(axis, result);
	}

	@Test
	void hash() {
		assertEquals(Objects.hash(Axis.class, "Axis"), axis.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, axis.equals(axis));
		assertEquals(true, axis.equals(new Axis("Axis")));
		assertEquals(false, axis.equals(null));
		assertEquals(false, axis.equals(new Axis("Other")));
	}

	@Nested
	class EventTests {
		private Axis.Event event;

		@BeforeEach
		void before() {
			event = axis.create(42);
		}

		@Test
		void constructor() {
			assertNotNull(event);
			assertEquals(axis, event.type());
			assertEquals(42, event.value());
		}

		@Test
		void equals() {
			assertEquals(true, event.equals(event));
			assertEquals(true, event.equals(axis.create(42)));
			assertEquals(false, event.equals(null));
			assertEquals(false, event.equals(axis.create(999)));
		}
	}
}
