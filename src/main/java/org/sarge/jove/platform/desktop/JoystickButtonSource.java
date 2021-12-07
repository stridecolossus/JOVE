package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.DefaultButton;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.control.Hat;

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
	private class JoystickButton extends DefaultButton {
		private byte current;

		/**
		 * Constructor.
		 * @param id			Button ID
		 * @param action		Initial action
		 */
		protected JoystickButton(int id, byte action) {
			super(Button.name("Button", id), Action.map(action), 0);
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
			final Button event = resolve(value, 0);
			handler.accept(event);
			current = value;
		}
	}

	private class JoystickHat extends Hat {
		private byte current;

		/**
		 * Constructor.
		 * @param id		Hat ID
		 * @param mask		Initial value
		 */
		public JoystickHat(int id, byte mask) {
			super(id, mask);
			this.current = mask;
		}

		void update(byte mask) {
			if(mask == current) {
				return;
			}

			final Hat event = resolve(mask);
			handler.accept(event);
			current = mask;
		}
	}

	private final int id;
	private final DesktopLibraryJoystick lib;
	private final JoystickButton[] buttons;
	private final JoystickHat[] hats;

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
	private JoystickHat[] initHats() {
		final byte[] bytes = getHatArray();
		return IntStream
				.range(0, bytes.length)
				.mapToObj(n -> new JoystickHat(n, bytes[n]))
				.toArray(JoystickHat[]::new);
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
