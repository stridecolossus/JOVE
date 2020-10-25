package org.sarge.jove.control;

/**
 * A <i>position event</i> describes a mouse or joystick positional movement.
 * @author Sarge
 */
public record Position(float x, float y) implements InputEvent {
	/**
	 * Singleton instance.
	 */
	public static final Type TYPE = new Type() {
		@Override
		public String name() {
			return "Position";
		}

		@Override
		public Type parse(String[] tokens) {
			return this;
		}

		@Override
		public int hashCode() {
			return name().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}

		@Override
		public String toString() {
			return name();
		}
	};

	@Override
	public Type type() {
		return TYPE;
	}
}
