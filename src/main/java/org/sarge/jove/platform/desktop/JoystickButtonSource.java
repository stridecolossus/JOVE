package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.AbstractButton;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Event source for joystick buttons.
 * @author Sarge
 */
public class JoystickButtonSource extends AbstractSource<Button> {
	/**
	 * Joystick button implementation.
	 */
	private class JoystickButton extends DesktopButton {
		private byte current;

		/**
		 * Constructor.
		 * @param id			Button ID
		 * @param action		Initial action
		 */
		protected JoystickButton(int id, byte action) {
			super(Button.name("Button", id), Action.map(action));
			this.current = action;
		}

		/**
		 * Updates the state of this button and generates events.
		 * @param value Button value
		 */
		void update(byte value) {
			// Ignore if not modified
			if(value == current) {
				return;
			}

			// Generate event
			final Button event = resolve(value);
			handler.accept(event);
			current = value;
		}
	}

	/**
	 * Hat actions.
	 * Note that hat diagonals are represented as a bit-mask, e.g. 3 for up-right.
	 */
	public enum HatAction implements IntegerEnumeration {
		CENTERED(0),
		UP(1),
		RIGHT(2),
		DOWN(4),
		LEFT(8);

		private static final IntegerEnumeration.ReverseMapping<HatAction> MAPPING = IntegerEnumeration.mapping(HatAction.class);
		private static final TreeSet<HatAction> EMPTY = new TreeSet<>(Set.of(CENTERED));

		private final int value;

		private HatAction(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	/**
	 * Joystick hat.
	 */
	public class Hat extends AbstractButton {
		private final int hat;
		private byte action;

		/**
		 * Constructor.
		 * @param hat			Hat ID
		 * @param action		Initial action
		 */
		Hat(int hat, byte action) {
			this.hat = zeroOrMore(id);
			this.action = action;
		}

		@Override
		public String name() {
			if(action == 0) {
				return Button.name("Hat", hat);
			}
			else {
				return Button.name("Hat", hat, action());
			}
		}

		@Override
		public Object action() {
			if(action == 0) {
				return HatAction.EMPTY;
			}
			else {
				return HatAction.MAPPING.enumerate(action);
			}
		}

		/**
		 * Updates the state of this hat and generates events.
		 * @param action Hat action
		 */
		void update(byte action) {
			// Ignore if not modified
			if(action == this.action) {
				return;
			}

			// Update hat
			this.action = action;

			// Generate event
			final Button event = new Hat(hat, action);
			handler.accept(event);
		}
	}

	private final int id;
	private final DesktopLibraryJoystick lib;
	private final JoystickButton[] buttons;
	private final Hat[] hats;

	/**
	 * Constructor.
	 * @param id		Joystick ID
	 * @param lib		GLFW library
	 */
	JoystickButtonSource(int id, DesktopLibraryJoystick lib) {
		this.id = zeroOrMore(id);
		this.lib = notNull(lib);
		this.buttons = initButtons();
		this.hats = initHats();
	}

	/**
	 * @return Joystick buttons
	 */
	private JoystickButton[] initButtons() {
		final byte[] bytes = getButtonArray();
		return IntStream
				.range(0, bytes.length)
				.mapToObj(n -> new JoystickButton(n, bytes[n]))
				.toArray(JoystickButton[]::new);
	}

	/**
	 * Queries the button values for this joystick.
	 */
	private byte[] getButtonArray() {
		final IntByReference count = new IntByReference();
		final Pointer ptr = lib.glfwGetJoystickButtons(id, count);
		return ptr.getByteArray(0, count.getValue());
	}

	/**
	 * @return Joystick buttons
	 */
	List<Button> buttons() {
		return Arrays.asList(buttons);
	}

	/**
	 * @return Joystick hats
	 */
	private Hat[] initHats() {
		final byte[] bytes = getHatArray();
		return IntStream
				.range(0, bytes.length)
				.mapToObj(n -> new Hat(n, bytes[n]))
				.toArray(Hat[]::new);
	}

	/**
	 * Queries the hat values for this joystick.
	 */
	private byte[] getHatArray() {
		final IntByReference count = new IntByReference();
		final Pointer ptr = lib.glfwGetJoystickHats(id, count);
		return ptr.getByteArray(0, count.getValue());
	}

	/**
	 * @return Joystick buttons
	 */
	List<Hat> hats() {
		return Arrays.asList(hats);
	}

	/**
	 * Polls for joystick button events.
	 */
	void poll() {
		// Ignore if no action handler
		if(handler == null) {
			return;
		}

		// Poll events
		pollButtons();
		pollHats();
	}

	/**
	 * Polls joystick buttons.
	 */
	private void pollButtons() {
		final byte[] values = getButtonArray();
		for(int n = 0; n < values.length; ++n) {
			buttons[n].update(values[n]);
		}
	}

	/**
	 * Polls joystick hats.
	 */
	private void pollHats() {
		final byte[] values = getHatArray();
		for(int n = 0; n < values.length; ++n) {
			hats[n].update(values[n]);
		}
	}
}
