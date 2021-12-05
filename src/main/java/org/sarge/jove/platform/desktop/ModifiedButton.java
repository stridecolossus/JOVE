package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Set;

import org.sarge.jove.control.Button;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>modified button</i> also
 * @author Sarge
 */
class ModifiedButton extends DesktopButton {
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

	private final int mods;

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Action
	 * @param mods			Modifier mask
	 */
	public ModifiedButton(String id, Action action, int mods) {
		super(id, action);
		this.mods = zeroOrMore(mods);
	}

	/**
	 * Constructor for a basic button.
	 * @param id Button identifier
	 */
	public ModifiedButton(String id) {
		this(id, Action.PRESS, 0);
	}

	@Override
	public String name() {
		final String modifiers = Button.name(modifiers().toArray());
		return Button.name(id, action.name(), modifiers);
	}

	/**
	 * @return Key modifiers
	 */
	public Set<Modifier> modifiers() {
		return IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
	}

	/**
	 * Creates this button with the given actions and modifier mask.
	 * @param action		Action code
	 * @param mods			Modifier mask
	 * @return New button
	 */
	public ModifiedButton resolve(int action, int mods) {
		return new ModifiedButton(id, Action.map(action), mods);
	}
}
