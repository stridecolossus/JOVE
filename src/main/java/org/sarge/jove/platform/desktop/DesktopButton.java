package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.AbstractButton;

/**
 * A <i>desktop button</i> represents GLFW keyboard keys and mouse buttons.
 * @author Sarge
 */
class DesktopButton extends AbstractButton {
	/**
	 * GLFW button actions.
	 */
	public enum Action {
		RELEASE,
		PRESS,
		REPEAT;

		private static final Action[] ACTIONS = Action.values();

		/**
		 * Maps a GLFW action code to this enumeration.
		 * @param action Action code
		 * @return Action
		 * @throws ArrayIndexOutOfBoundsException for an invalid action code
		 */
		public static Action map(int action) {
			return ACTIONS[action];
		}
	}

	protected final String id;
	protected final Action action;

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Action
	 */
	protected DesktopButton(String id, Action action) {
		this.id = notEmpty(id);
		this.action = notNull(action);
	}

	/**
	 * Constructor for a button.
	 * @param id Button identifier
	 */
	public DesktopButton(String id) {
		this(id, Action.RELEASE);
	}

	@Override
	public String name() {
		return Button.name(id, action.name());
	}

	@Override
	public final Action action() {
		return action;
	}

	/**
	 * Resolves this button for the given action.
	 * @param action Action
	 * @return Resolved button
	 */
	DesktopButton resolve(Action action) {
		return new DesktopButton(id, action);
	}
}
