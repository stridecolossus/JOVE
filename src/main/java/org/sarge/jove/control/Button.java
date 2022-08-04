package org.sarge.jove.control;

import static org.sarge.lib.util.Check.*;

import java.util.Objects;
import java.util.function.Consumer;

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

	// TODO
	public static Consumer<Button<Action>> handler(Runnable action) {
		return ignored -> action.run();
	}

	private final Source<Button<T>> source;
	private final String id;
	private final T action;

	/**
	 * Constructor.
	 * @param source		Event source for this button
	 * @param id			Button identifier
	 * @param action		Button action
	 */
	public Button(Source<Button<T>> source, String id, T action) {
		this.source = notNull(source);
		this.id = notEmpty(id);
		this.action = notNull(action);
	}

	@Override
	public Source<Button<T>> source() {
		return source;
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
		return Event.name(source.name(), id, action);
	}

	/**
	 * @return Button action
	 */
	public T action() {
		return action;
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, id, action);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Button<?> that) &&
				this.id.equals(that.id) &&
				this.action.equals(that.action) &&
				this.source.equals(that.source);
	}

	@Override
	public String toString() {
		return name();
	}
}
