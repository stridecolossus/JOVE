package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.control.Button.AbstractButton;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>default button</i> represents keyboard keys and mouse buttons.
 * TODO
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

	/**
	 * Button modifiers.
	 */
	public enum Modifier implements IntegerEnumeration {
		SHIFT(0x0001),
		CONTROL(0x0002),
		ALT(0x0004),
		SUPER(0x0008),
		CAPS_LOCK(0x0010),
		NUM_LOCK(0x0020);

		private final int value;

		private Modifier(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	private final String id;
	private final String name;
	private final Action action;
	private final Set<Modifier> mods;

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Action
	 * @param mods			Modifier mask
	 */
	protected DefaultButton(String id, Action action, int mods) {
		this.id = notEmpty(id);
		this.action = notNull(action);
		this.mods = IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
		this.name = build();
	}

	/**
	 * Constructor for an unmodified button.
	 * @param id Button identifier
	 */
	public DefaultButton(String id) {
		this(id, Action.PRESS, 0);
	}

	/**
	 * Constructs the name of this button.
	 * @return Button name
	 */
	private String build() {
		final String str = Button.name(mods.toArray());
		return Button.name(id, action, str);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Action action() {
		return action;
	}

	/**
	 * @return Button modifiers
	 */
	public Set<Modifier> modifiers() {
		return mods;
	}

	/**
	 * Resolves this button.
	 * @param action	Action
	 * @param mods		Modifier mask
	 * @return Resolved button
	 */
	public DefaultButton resolve(int action, int mods) {
		return new DefaultButton(id, Action.map(action), mods);
	}
}
