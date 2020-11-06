package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Action.Bindings;

public class ActionTest {
	private Bindings<Axis> bindings;
	private Axis axis;
	private Action<Axis> action;
	private Axis.Event event;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		bindings = new Bindings<>();
		axis = new Axis("Axis");
		action = mock(Action.class);
		event = axis.create(42);
	}

	@Test
	void constructor() {
		assertNotNull(bindings.actions());
		assertEquals(0, bindings.actions().count());
	}

	@Test
	void add() {
		bindings.add(action);
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
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
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
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
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
	}

	@Test
	void removeAction() {
		bindings.bind(axis, action);
		bindings.remove(action);
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
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
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
	}

	@Test
	void accept() {
		bindings.bind(axis, action);
		bindings.accept(event);
		verify(action).accept(event);
	}

	@Test
	void runnable() {
		// Bind an anonymous event
		final Runnable runnable = mock(Runnable.class);
		bindings.bind(axis, runnable);

		// Check action wrapper is bound
		final Optional<Action<Axis>> wrapper = bindings.binding(axis);
		assertNotNull(wrapper);
		assertEquals(true, wrapper.isPresent());

		// Invoke action wrapper
		wrapper.get().accept(event);
		verify(runnable).run();
	}

	// TODO - save/load
}
