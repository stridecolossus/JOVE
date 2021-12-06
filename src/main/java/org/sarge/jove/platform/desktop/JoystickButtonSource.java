package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.AbstractSource;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Event source for joystick buttons.
 * @author Sarge
 */
class JoystickButtonSource extends AbstractSource<Button> {
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
		private JoystickButton(int id, byte action) {
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
			final Button event = resolve(Action.map(value));
			handler.accept(event);
			current = value;
		}
	}

	private final int id;
	private final DesktopLibraryJoystick lib;
	private final JoystickButton[] buttons;

	/**
	 * Constructor.
	 * @param id		Joystick ID
	 * @param lib		GLFW library
	 */
	JoystickButtonSource(int id, DesktopLibraryJoystick lib) {
		this.id = zeroOrMore(id);
		this.lib = notNull(lib);
		this.buttons = create();
	}

	/**
	 * @return Joystick buttons
	 */
	private JoystickButton[] create() {
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
	public List<Button> buttons() {
		return Arrays.asList(buttons);
	}

	/**
	 * Polls for joystick button events.
	 */
	void poll() {
		// Ignore if no action handler
		if(handler == null) {
			return;
		}

		// Retrieve button values
		final byte[] values = getButtonArray();

		// Generate events for modified buttons
		for(int n = 0; n < values.length; ++n) {
			buttons[n].update(values[n]);
		}
	}
}
