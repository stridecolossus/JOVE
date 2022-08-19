package org.sarge.jove.control;

/**
 * A <i>position event</i> represents a 2D movement event such as the mouse pointer.
 * @author Sarge
 */
public record Position(float x, float y) implements Event {
	@Override
	public boolean matches(Event e) {
		return e instanceof Position;
	}

	@Override
	public String toString() {
		return String.format("%f,%f", x, y);
	}
}
