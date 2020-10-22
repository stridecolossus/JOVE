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
			assertEquals(Category.MOVE, WindowDescriptor.MOVE.category());
			assertEquals(0, WindowDescriptor.MOVE.id());
			assertEquals(null, WindowDescriptor.MOVE.operation());
			assertEquals(WindowDescriptor.MOVE, WindowDescriptor.parse("MOVE"));
		}

		@Test
		public void zoom() {
			assertEquals(Category.ZOOM, WindowDescriptor.ZOOM.category());
			assertEquals(0, WindowDescriptor.ZOOM.id());
			assertEquals(null, WindowDescriptor.ZOOM.operation());
			assertEquals(WindowDescriptor.ZOOM, WindowDescriptor.parse("ZOOM"));
		}

		@Test
		public void button() {
			final WindowDescriptor descriptor = new WindowDescriptor(Category.BUTTON, 42, Operation.PRESS);
			assertNotNull(descriptor);
			assertEquals(Category.BUTTON, descriptor.category());
			assertEquals(42, descriptor.id());
			assertEquals(Operation.PRESS, descriptor.operation());
			assertEquals(descriptor, WindowDescriptor.parse("BUTTON-PRESS-42"));
		}

		@Test
		public void click() {
			final WindowDescriptor descriptor = new WindowDescriptor(Category.CLICK, 42, Operation.PRESS);
			assertNotNull(descriptor);
			assertEquals(Category.CLICK, descriptor.category());
			assertEquals(42, descriptor.id());
			assertEquals(Operation.PRESS, descriptor.operation());
			assertEquals(descriptor, WindowDescriptor.parse("CLICK-PRESS-42"));
		}

		@Test
		public void invalidNamedCategory() {
			assertThrows(IllegalArgumentException.class, () -> new WindowDescriptor(Category.ZOOM, 42, Operation.PRESS));
			assertThrows(IllegalArgumentException.class, () -> new WindowDescriptor(Category.MOVE, 42, Operation.PRESS));
		}

		@Test
		public void invalidClickOperation() {
			assertThrows(IllegalArgumentException.class, () -> new WindowDescriptor(Category.CLICK, 42, Operation.REPEAT));
		}

		@Test
		public void parseInvalid() {
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse(""));
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse("cobblers"));
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse("ZOOM-cobblers"));
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse("BUTTON"));
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse("BUTTON-PRESS"));
			assertThrows(IllegalArgumentException.class, () -> WindowDescriptor.parse("BUTTON-PRESS-42-cobblers"));
		}
	}

	@Nested
	class EventTests {
		@Test
		public void move() {
			final Event event = new Event(WindowDescriptor.MOVE, 1, 2);
			assertNotNull(event);
			assertEquals(WindowDescriptor.MOVE, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void zoom() {
			final Event event = new Event(WindowDescriptor.ZOOM, 1, 2);
			assertNotNull(event);
			assertEquals(WindowDescriptor.ZOOM, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void button() {
			final WindowDescriptor descriptor = new WindowDescriptor(Category.BUTTON, 42, Operation.PRESS);
			final Event event = new Event(descriptor);
			assertNotNull(event);
			assertEquals(descriptor, event.descriptor());
			assertEquals(0, event.x);
			assertEquals(0, event.y);
		}

		@Test
		public void click() {
			final WindowDescriptor descriptor = new WindowDescriptor(Category.CLICK, 42, Operation.PRESS);
			final Event event = new Event(descriptor, 1, 2);
			assertNotNull(event);
			assertEquals(descriptor, event.descriptor());
			assertEquals(1, event.x);
			assertEquals(2, event.y);
		}

		@Test
		public void invalidEvent() {
			final WindowDescriptor click = new WindowDescriptor(Category.CLICK, 42, Operation.PRESS);
			assertThrows(IllegalArgumentException.class, () -> new Event(click));
			assertThrows(IllegalArgumentException.class, () -> new Event(WindowDescriptor.MOVE));
			assertThrows(IllegalArgumentException.class, () -> new Event(WindowDescriptor.ZOOM));
		}

		@Test
		public void invalidLocationEvent() {
			final WindowDescriptor button = new WindowDescriptor(Category.BUTTON, 42, Operation.PRESS);
			assertThrows(IllegalArgumentException.class, () -> new Event(button, 1, 2));
		}
	}
}
