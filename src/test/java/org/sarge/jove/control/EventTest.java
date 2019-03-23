package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Key;

public class EventTest {
	private Event.Key key;

	@BeforeEach
	public void before() {
		key = Event.Key.of(Event.Category.BUTTON, Event.Type.PRESS, 42);
	}

	@Nested
	class KeyTests {
		@Test
		public void constructor() {
			assertEquals(Event.Category.BUTTON, key.category());
			assertEquals(Event.Type.PRESS, key.type());
			assertEquals(42, key.identifier());
		}

		@Test
		public void invalidNullType() {
			assertThrows(NullPointerException.class, () -> Event.Key.of(Event.Category.BUTTON, null, 42));
		}

		@Test
		public void invalidIdentifier() {
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> Event.Key.of(Event.Category.BUTTON, Event.Type.PRESS, -1));
		}

		@Test
		public void event() {
			assertNotNull(key.event());
			assertEquals(null, Event.Key.MOVE.event());
			assertEquals(null, Event.Key.ZOOM.event());
		}

		@Test
		public void equals() {
			assertEquals(true, key.equals(key));
			assertEquals(false, key.equals(null));
			assertEquals(false, key.equals(Event.Key.of(Event.Category.BUTTON, Event.Type.PRESS, 999)));
		}

		@Test
		public void string() {
			assertEquals("BUTTON-PRESS-42", key.toString());
		}

		@Test
		public void parse() {
			assertEquals(key, Key.parse("BUTTON-PRESS-42"));
		}
	}

	@Nested
	class EventTests {
		@Test
		public void constructor() {
			final Event event = new Event(Event.Key.MOVE, 1, 2);
			assertEquals(Event.Key.MOVE, event.key());
			assertEquals(1, event.x());
			assertEquals(2, event.y());
		}

		@Test
		public void constructorSuperfluousLocation() {
			assertThrows(IllegalArgumentException.class, () -> new Event(key, 1, 2));
		}
	}
}
