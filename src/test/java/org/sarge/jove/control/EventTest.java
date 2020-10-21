package org.sarge.jove.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Category;

public class EventTest {
	@Nested
	class DescriptorTests {
		@Test
		public void move() {
			assertEquals(Category.MOVE, Descriptor.MOVE.category());
			assertEquals(0, Descriptor.MOVE.id());
			assertEquals(null, Descriptor.MOVE.operation());
			assertEquals(Descriptor.MOVE, Descriptor.parse("MOVE"));
		}

		@Test
		public void zoom() {
			assertEquals(Category.ZOOM, Descriptor.ZOOM.category());
			assertEquals(0, Descriptor.ZOOM.id());
			assertEquals(null, Descriptor.ZOOM.operation());
			assertEquals(Descriptor.ZOOM, Descriptor.parse("ZOOM"));
		}

		@Test
		public void button() {
			final Descriptor descriptor = new Descriptor(Category.BUTTON, 42, Operation.PRESS);
			assertNotNull(descriptor);
			assertEquals(Category.BUTTON, descriptor.category());
			assertEquals(42, descriptor.id());
			assertEquals(Operation.PRESS, descriptor.operation());
			assertEquals(descriptor, Descriptor.parse("BUTTON-PRESS-42"));
		}

		@Test
		public void click() {
			final Descriptor descriptor = new Descriptor(Category.CLICK, 42, Operation.PRESS);
			assertNotNull(descriptor);
			assertEquals(Category.CLICK, descriptor.category());
			assertEquals(42, descriptor.id());
			assertEquals(Operation.PRESS, descriptor.operation());
			assertEquals(descriptor, Descriptor.parse("CLICK-PRESS-42"));
		}

		@Test
		public void invalidNamedCategory() {
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(Category.ZOOM, 42, Operation.PRESS));
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(Category.MOVE, 42, Operation.PRESS));
		}

		@Test
		public void invalidClickOperation() {
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(Category.CLICK, 42, Operation.REPEAT));
		}

		@Test
		public void parseInvalid() {
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse(""));
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse("cobblers"));
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse("ZOOM-cobblers"));
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse("BUTTON"));
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse("BUTTON-PRESS"));
			assertThrows(IllegalArgumentException.class, () -> Descriptor.parse("BUTTON-PRESS-42-cobblers"));
		}
	}

	@Nested
	class EventTests {
		@Test
		public void move() {
			final Event event = new Event(Descriptor.MOVE, 1, 2);
			assertNotNull(event);
			assertEquals(Descriptor.MOVE, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void zoom() {
			final Event event = new Event(Descriptor.ZOOM, 1, 2);
			assertNotNull(event);
			assertEquals(Descriptor.ZOOM, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void button() {
			final Descriptor descriptor = new Descriptor(Category.BUTTON, 42, Operation.PRESS);
			final Event event = new Event(descriptor);
			assertNotNull(event);
			assertEquals(descriptor, event.descriptor());
			assertEquals(0, event.x);
			assertEquals(0, event.y);
		}

		@Test
		public void click() {
			final Descriptor descriptor = new Descriptor(Category.CLICK, 42, Operation.PRESS);
			final Event event = new Event(descriptor, 1, 2);
			assertNotNull(event);
			assertEquals(descriptor, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void invalidEvent() {
			final Descriptor click = new Descriptor(Category.CLICK, 42, Operation.PRESS);
			assertThrows(IllegalArgumentException.class, () -> new Event(click));
			assertThrows(IllegalArgumentException.class, () -> new Event(Descriptor.MOVE));
			assertThrows(IllegalArgumentException.class, () -> new Event(Descriptor.ZOOM));
		}

		@Test
		public void invalidLocationEvent() {
			final Descriptor button = new Descriptor(Category.BUTTON, 42, Operation.PRESS);
			assertThrows(IllegalArgumentException.class, () -> new Event(button, 1, 2));
		}
	}
}
