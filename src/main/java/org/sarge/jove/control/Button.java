package org.sarge.jove.control;

import static org.sarge.lib.util.Check.*;

import java.util.Objects;

/**
 * A <i>button</i> is an event for a keyboard, mouse, or joystick button.
 * @param <T> Button action type
 * @author Sarge
 */
public class Button<T> implements Event {
	/**
	 * Default actions based on the GLFW action codes.
	 */
	public enum Action {
		RELEASE,
		PRESS,
		REPEAT;

		private static final Action[] ACTIONS = Action.values();

		/**
		 * Maps a GLFW action code to the corresponding constant.
		 * @param action Action code
		 * @return Action
		 * @throws ArrayIndexOutOfBoundsException for an invalid action code
		 */
		public static Action map(int action) {
			return ACTIONS[action];
		}
	}

	private final String id;
	private final T action;

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Button action
	 */
	public Button(String id, T action) {
		this.id = notEmpty(id);
		this.action = notNull(action);
	}

	/**
	 * @return Button identifier
	 */
	public String id() {
		return id;
	}

	/**
	 * @return Hyphen-delimited name of this button event
	 */
	public String name() {
		return Event.name(id, action);
	}

	/**
	 * @return Button action
	 */
	public T action() {
		return action;
	}

	@Override
	public boolean matches(Event e) {
		// TODO
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, action);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Button<?> that) &&
				this.id.equals(that.id) &&
				this.action.equals(that.action);
	}

	@Override
	public String toString() {
		return name();
	}
}
