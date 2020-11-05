package org.sarge.jove.control;

import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>position event</i> describes a mouse or joystick positional movement.
 * @author Sarge
 */
public final class Position implements Type {
	private static final String NAME = "Position";

	/**
	 * Singleton instance.
	 */
	public static final Position TYPE = new Position(NAME);

	private Position() {
	}

	/**
	 * Constructor.
	 * @param name Name (ignored)
	 */
	private Position(String name) {
		assert this.name().equals(name);
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Position;
	}

	/**
	 * Position event instance.
	 */
	public record Event(float x, float y) implements InputEvent<Position> {
		@Override
		public Type type() {
			return TYPE;
		}
	}
}
