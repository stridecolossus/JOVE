package org.sarge.jove.control;

import java.util.Set;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>modified button</i> is a button with a keyboard <i>modifiers</i> mask.
 * @author Sarge
 */
public class ModifiedButton extends DefaultButton {
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

	private final Set<Modifier> mods;

	/**
	 * Constructor for an unmodified button.
	 * @param id Button identifier
	 */
	public ModifiedButton(String id) {
		this(id, Action.RELEASE, 0);
	}

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Action
	 * @param mods			Modifiers mask
	 */
	protected ModifiedButton(String id, Action action, int mods) {
		super(id, action);
		this.mods = IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
	}

	@Override
	public String name() {
		if(mods.isEmpty()) {
			return super.name();
		}
		else {
			final String str = Button.name(mods.toArray());
			return Button.name(super.name(), str);
		}
	}

	/**
	 * @return Button modifiers
	 */
	public Set<Modifier> modifiers() {
		return mods;
	}

	@Override
	public ModifiedButton resolve(int action, int mods) {
		return new ModifiedButton(id, Action.map(action), mods);
	}
}
