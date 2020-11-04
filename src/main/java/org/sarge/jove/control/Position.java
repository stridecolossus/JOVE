package org.sarge.jove.control;

import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>position event</i> describes a mouse or joystick positional movement.
 * @author Sarge
 */
public final class Position implements Type {
	/**
	 * Singleton instance.
	 */
	public static final Type TYPE = new Position();

	/**
	 * Parser.
	 * @param str String representation (ignored)
	 * @return Position type
	 */
	static Type parse(String str) {
		if(!TYPE.name().equals(str)) throw new IllegalArgumentException();
		return TYPE;
	}

	private Position() {
	}

	@Override
	public String name() {
		return "Position";
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
