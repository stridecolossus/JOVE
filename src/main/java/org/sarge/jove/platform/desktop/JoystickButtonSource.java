package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private final int id;
	private final DesktopLibraryJoystick lib;
	private final Button[] buttons;
	private final Hat[] hats;
	private final Map<Button, Byte> values = new HashMap<>();

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
	private Button[] initButtons() {
		// Retrieve initial button values
		final byte[] values = getButtonArray();

		// Create buttons
		final Button[] buttons = IntStream
				.range(0, values.length)
				.mapToObj(id -> Button.name("Button", id))
				.map(DefaultButton::new)
				.toArray(Button[]::new);

		// Init button states
		init(buttons);
		update(values, buttons);

		return buttons;
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
		// Retrieve hat values
		final byte[] values = getHatArray();

		// Create hats
		final Hat[] hats = IntStream
				.range(0, values.length)
				.mapToObj(id -> Button.name("Hat", id))
				.map(Hat::new)
				.toArray(Hat[]::new);

		// Init hat values
		init(hats);
		update(values, hats);

		return hats;
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
		update(getButtonArray(), buttons);
		update(getHatArray(), hats);
	}

	/**
	 * Initialises the cached values for the given buttons.
	 */
	private void init(Button[] buttons) {
		final Byte released = 0;
		for(Button b : buttons) {
			values.put(b, released);
		}
	}

	/**
	 * Updates buttons and hats.
	 * @param values		New button values
	 * @param buttons		Buttons to update
	 */
	private void update(byte[]values, Button[] buttons) {
		for(int n = 0; n < values.length; ++n) {
			update(buttons[n], values[n]);
		}
	}

	/**
	 * Updates a button and generates events.
	 * @param button		Button
	 * @param value			New value
	 */
	private void update(Button button, byte value) {
		// Skip if not modified
		final Byte prev = values.get(button);
		if(prev == value) {
			return;
		}

		// Update state
		values.put(button, value);

		// Ignore if no event handler
		if(handler == null) {
			return;
		}

		// Generate event
		final Button event = button.resolve(value);
		handler.accept(event);
	}
}
