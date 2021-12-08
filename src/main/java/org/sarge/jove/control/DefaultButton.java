package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.control.Button.AbstractButton;

/**
 * A <i>default button</i> represents keyboard keys and mouse buttons.
 * @author Sarge
 */
public class DefaultButton extends AbstractButton {
	/**
	 * GLFW button actions.
	 */
	public enum Action {
		RELEASE,
		PRESS,
		REPEAT;

		private static final Action[] ACTIONS = Action.values();

		/**
		 * Maps an action code to this enumeration.
		 * @param action Action code
		 * @return Action
		 * @throws ArrayIndexOutOfBoundsException for an invalid action code
		 */
		public static Action map(int action) {
			return ACTIONS[action];
		}
	}

	protected final String id;
	private final Action action;

	/**
	 * Constructor.
	 * @param id Button identifier
	 */
	public DefaultButton(String id) {
		this(id, Action.RELEASE);
	}

	/**
	 * Constructor.
	 * @param id 			Button identifier
	 * @param action		Button action
	 */
	protected DefaultButton(String id, Action action) {
		this.id = notEmpty(id);
		this.action = notNull(action);
	}

	@Override
	public String name() {
		return Button.name(id, action);
	}

	@Override
	public Action action() {
		return action;
	}

	@Override
	public DefaultButton resolve(int action, int mods) {
		checkUnmodified(mods);
		return new DefaultButton(id, Action.map(action));
	}
}
