package org.sarge.jove.control;

import java.util.Set;

import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>modified button</i> represents keyboard keys and mouse buttons with an additional keyboard modifier mask.
 * <p>
 * When used as a template both the <i>action</i> and keyboard <i>modifiers</i> are optional:
 * <pre>
 * new ModifiedButton("id", Action.PRESS, 0x01);	// Match specific action and modifier mask
 * new ModifiedButton("id", null, 0x01);		// Match mask
 * new ModifiedButton("id", Action.PRESS, 0);	// Match action
 * new ModifiedButton("id", null, 0);		// Match any action or mask
 * </pre>
 * Also note that a button with a super-set of the specified modifier mask is considered a matching button.
 * In the following example both matches would pass:
 * <pre>
 * ModifiedButton template = new ModifiedButton("id", null, 0x001);
 * template.match(new ModifiedButton("id", Action.PRESS, 0x001));
 * template.match(new ModifiedButton("id", Action.PRESS, 0x011));
 * </pre>
 * The {@link #resolve(int, int)} helper can be used to derived a button with a specified action and modifier mask.
 * <p>
 * @author Sarge
 */
public class ModifiedButton extends DefaultButton {
	/**
	 * Keyboard modifiers for this button.
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
	 * Constructor for a button without modifiers.
	 * @param id Button identifier
	 */
	public ModifiedButton(String id) {
		this(id, Action.PRESS, 0);
	}

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Optional action
	 * @param mods			Keyboard modifiers mask
	 */
	protected ModifiedButton(String id, Action action, int mods) {
		super(id, action);
		this.mods = mods;
		assert modifiers() != null;		// Validate mask
	}

	@Override
	public String name() {
		if(mods == 0) {
			return super.name();
		}
		else {
			final Set<Modifier> set = modifiers();
			final String str = Button.name(set.toArray());
			return Button.name(super.name(), str);
		}
	}

	/**
	 * @return Button modifiers
	 */
	public Set<Modifier> modifiers() {
		return IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
	}

	@Override
	public boolean matches(Button button) {
		return
				super.matches(button) &&
				(button instanceof ModifiedButton that) &&
				MathsUtil.isMask(that.mods, this.mods);
	}

	@Override
	public ModifiedButton resolve(int action) {
		return resolve(action, mods);
	}

	/**
	 * Helper - Resolves this button for the given action and keyboard modifiers mask.
	 * @param action		Action code
	 * @param mods			Keyboard modifiers mask
	 * @return Resolved button
	 */
	public ModifiedButton resolve(int action, int mods) {
		return new ModifiedButton(id, Action.map(action), mods);
	}
}
