package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import java.util.Set;

import org.sarge.jove.util.*;

/**
 * A <i>button</i> is a keyboard key, mouse button, or controller button.
 * @author Sarge
 */
public record Button(int key, String name) {
	/**
	 * Constructor.
	 * @param key		Button key code
	 * @param name		Identifier
	 */
	public Button {
		requireZeroOrMore(key);
		requireNotEmpty(name);
	}

	/**
	 * Button actions.
	 */
	public enum ButtonAction {
		RELEASE,
		PRESS,
		REPEAT;

		/**
		 * Maps a GLFW action code.
		 * @param action Action code
		 * @return Action
		 * @throws IllegalArgumentException for an invalid action code
		 */
		public static ButtonAction map(int action) {
			return switch(action) {
				case 0 -> RELEASE;
				case 1 -> PRESS;
				case 2 -> REPEAT;
				default -> throw new IllegalArgumentException("Invalid action code: " + action);
			};
		}
	}

	/**
	 * Modifier keys.
	 */
	public enum ModifierKey implements IntEnum {
		SHIFT(0x001),
		CONTROL(0x002),
		ALT(0x004),
		SUPER(0x008),
		CAPS_LOCK(0x010),
		NUM_LOCK(0x020);

		private static final ReverseMapping<ModifierKey> MAPPING = ReverseMapping.mapping(ModifierKey.class);

		private final int bit;

		private ModifierKey(int bit) {
			this.bit = bit;
		}

		@Override
		public int value() {
			return bit;
		}

		/**
		 * Maps the modifier keys from the given bit-field.
		 * @param bits Modifier keys bit-field
		 * @return Modifier keys
		 */
		public static Set<ModifierKey> map(int bits) {
			final var mask = new EnumMask<ModifierKey>(bits);
			return mask.enumerate(MAPPING);
		}
	}

	/**
	 * A <i>button event</i> specifies the action and modifiers for a button event.
	 */
	public record ButtonEvent(Button button, ButtonAction action, Set<ModifierKey> modifiers) implements Event {
		/**
		 * Constructor.
		 * @param button		Button
		 * @param action		Button action
		 * @param modifiers		Modifier keys
		 */
		public ButtonEvent {
			requireNonNull(button);
			requireNonNull(action);
			modifiers = Set.copyOf(modifiers);
		}
	}
}
