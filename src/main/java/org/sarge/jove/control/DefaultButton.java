package org.sarge.jove.control;

import java.util.Objects;

import org.sarge.jove.control.Button.AbstractButton;

/**
 * A <i>default button</i> represents keyboard keys and mouse buttons.
 * <p>
 * When used as a template the <i>action</i> is optional:
 * <pre>
 * new ModifiedButton("id", Action.PRESS);		// Match specific action and modifier mask
 * new ModifiedButton("id", null);				// Match any action
 * </pre>
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

	private final Action action;

	/**
	 * Constructor.
	 * @param id Button identifier
	 */
	public DefaultButton(String id) {
		this(id, null);
	}

	/**
	 * Constructor.
	 * @param id 			Button identifier
	 * @param action		Button action or {@code null} for a template that matches <b>all</b> actions
	 */
	protected DefaultButton(String id, Action action) {
		super(id);
		this.action = action;
	}

	@Override
	public String name() {
		return Button.name(id, action());
	}

	@Override
	public Action action() {
		if(action == null) {
			return Action.PRESS;
		}
		else {
			return action;
		}
	}

	@Override
	public boolean matches(Button button) {
		if(!Objects.isNull(action) && !action.equals(button.action())) {
			return false;
		}

		return super.matches(button);
	}

	@Override
	public DefaultButton resolve(int action) {
		return new DefaultButton(id, Action.map(action));
	}
}
