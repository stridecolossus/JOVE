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
		axis = new Axis("Axis", 42);
	}

	@Test
	void constructor() {
		assertEquals("Axis", axis.prefix());
		assertEquals(42, axis.id());
		assertEquals("Axis-42", axis.name());
	}

	@Test
	void parse() {
		assertEquals(axis, axis.parse(new String[]{"Axis", "42"}));
	}

	@Test
	void hash() {
		assertEquals(Objects.hash("Axis", 42), axis.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, axis.equals(axis));
		assertEquals(true, axis.equals(new Axis("Axis", 42)));
		assertEquals(false, axis.equals(null));
		assertEquals(false, axis.equals(new Axis("Axis", 999)));
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
