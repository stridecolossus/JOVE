package org.sarge.jove.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Axis;

public class InputEventTestOLD {
	@Test
	void position() {
		// Check position description
		assertEquals("Position", Type.POSITION.toString());
		assertEquals(Type.POSITION, Type.parse("Position"));

		// Create position event
		final InputEvent event = Type.POSITION.create(1, 2);
		assertNotNull(event);
		assertEquals(Type.POSITION, event.type());
		assertEquals(1, event.x());
		assertEquals(2, event.y());

		// Check equality
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(Type.POSITION.create(1, 2)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(Type.POSITION.create(3, 4)));
	}

	@Test
	void button() {
		// Check button description
		final Button button = new Button(1, 2, 3);
		assertEquals("Button-1", button.toString());
		// TODO - assertEquals(button, Type.parse("Button-1-name"));

		// Create button event
		final InputEvent event = button.event();
		assertNotNull(event);
		assertEquals(button, event.type());
		assertEquals(0, event.x());
		assertEquals(0, event.y());

		// Check equality
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(button.event()));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(new Button(4, 5, 6).event()));
	}

	@Test
	void axis() {
		// Check axis description
		final Axis axis = new Axis(1);
		assertEquals("Axis-1", axis.toString());
		assertEquals(axis, Type.parse("Axis-1"));

		// Create axis event
		final InputEvent event = axis.create(2);
		assertNotNull(event);
		assertEquals(axis, event.type());
		assertEquals(0, event.x());
		assertEquals(2, event.y());

		// Check equality
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(axis.create(2)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(axis.create(3)));
	}
}
