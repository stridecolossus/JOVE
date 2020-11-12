package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Handler;

public class BindingsTest {
	private Bindings bindings;
	private Axis axis;
	private Handler action;
	private Axis.Event event;

	@BeforeEach
	void before() {
		// Create an event
		axis = new Axis("axis");
		event = axis.create(42);

		// Create an action
		action = mock(Handler.class);
		when(action.toString()).thenReturn("action");

		// Create bindings
		bindings = new Bindings();
	}

	@Test
	void constructor() {
		assertNotNull(bindings.actions());
		assertEquals(0, bindings.actions().count());
	}

	@Test
	void add() {
		bindings.add(action);
		assertArrayEquals(new Handler[]{action}, bindings.actions().toArray());
	}

	@Test
	void addDuplicate() {
		bindings.add(action);
		assertThrows(IllegalArgumentException.class, () -> bindings.add(action));
	}

	@Test
	void bindings() {
		bindings.add(action);
		assertNotNull(bindings.bindings(action));
		assertEquals(0, bindings.bindings(action).count());
	}

	@Test
	void bindingNotPresent() {
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void bind() {
		bindings.bind(axis, action);
		assertArrayEquals(new Handler[]{action}, bindings.actions().toArray());
		assertArrayEquals(new Axis[]{axis}, bindings.bindings(action).toArray());
		assertEquals(Optional.of(action), bindings.binding(axis));
	}

	@Test
	void bindAlreadyBound() {
		bindings.bind(axis, action);
		assertThrows(IllegalStateException.class, () -> bindings.bind(axis, action));
	}

	@Test
	void remove() {
		bindings.bind(axis, action);
		bindings.remove(axis);
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
		assertArrayEquals(new Handler[]{action}, bindings.actions().toArray());
	}

	@Test
	void removeAction() {
		bindings.bind(axis, action);
		bindings.remove(action);
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
		assertArrayEquals(new Handler[]{action}, bindings.actions().toArray());
	}

	@Test
	void removeActionNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(action));
	}

	@Test
	void clear() {
		bindings.bind(axis, action);
		bindings.clear();
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
		assertArrayEquals(new Handler[]{action}, bindings.actions().toArray());
	}

	@Test
	void accept() {
		bindings.bind(axis, action);
		bindings.accept(event);
		verify(action).accept(event);
	}

	@Nested
	class LoaderTests {
		@BeforeEach
		void before() {
			bindings.bind(axis, action);
		}

		@Test
		void save() {
			final StringWriter out = new StringWriter();
			bindings.save(out);
			assertEquals("action Axis-axis", out.toString().trim());
		}

		@Test
		void load() throws IOException {
			// Save bindings
			final StringWriter out = new StringWriter();
			bindings.save(out);

			// Re-load bindings
			bindings.clear();
			bindings.load(new StringReader(out.toString()));

			// Check binding
			assertEquals(Optional.of(action), bindings.binding(axis));
		}

		@Test
		void loadUnknownAction() {
			assertThrows(IllegalArgumentException.class, "Action not present", () -> bindings.load(new StringReader("cobblers")));
		}

		@Test
		void loadInvalidBinding() {
			assertThrows(IllegalArgumentException.class, "Invalid event binding", () -> bindings.load(new StringReader("action cobblers")));
		}

		@Test
		void loadUnknownEventType() {
			assertThrows(IllegalArgumentException.class, "Invalid event type", () -> bindings.load(new StringReader("action cobblers-whatever")));
		}
	}
}
