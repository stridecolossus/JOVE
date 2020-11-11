package org.sarge.jove.control;

import static org.sarge.jove.util.Check.notEmpty;

import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>position event</i> describes a mouse or joystick positional movement.
 * @author Sarge
 */
public final class Position implements Type {
	/**
	 * Parses a position from its string representation.
	 * @param name String representation
	 * @return New position
	 */
	public static Position parse(String name) {
		return new Position(name);
	}

	private final String name;

	/**
	 * Constructor.
	 * @param name Position name
	 */
	public Position(String name) {
		this.name = notEmpty(name);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Position that) && this.name.equals(that.name);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Position event instance.
	 */
	public record Event(Position type, float x, float y) implements InputEvent {
		// Empty
	}

//	public interface Handler extends Action<Event> {
//		void handle(float x, float y);
//
//		static Action<Event> of(Handler handler) {
//			return event -> handler.handle(event.x, event.y);
//		}
//	}
}
