package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.Source;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Event source for joystick buttons.
 * @author Sarge
 */
class JoystickButtonSource implements Source<Button> {
	private final int id;
	private final Desktop desktop;
	private final Button[] buttons;
//	private final Hat[] hats;
	private final Map<Button, Byte> values = new HashMap<>();

	/**
	 * Constructor.
	 * @param id		Joystick ID
	 * @param desktop	Desktop service
	 */
	JoystickButtonSource(int id, Desktop desktop) {
		this.id = zeroOrMore(id);
		this.desktop = notNull(desktop);
		this.buttons = initButtons();
//		this.hats = initHats();
	}

	@Override
	public String name() {
		return "Joystick-Buttons";
	}

	@Override
	public void bind(Consumer<Button> handler) {
	}

	/**
	 * @return Joystick buttons
	 */
	private Button[] initButtons() {
		// Retrieve initial button values
		final byte[] values = getButtonArray();

		// Create buttons
		final String[] buttons = IntStream
				.range(0, values.length)
				.mapToObj(id -> Button.name("Button", id))
				.toArray(String[]::new);

//		// Init button states
//		init(buttons);
//		update(values, buttons);
//
//		return buttons;
		return null;
	}

	/**
	 * Queries the button values for this joystick.
	 */
	private byte[] getButtonArray() {
		final IntByReference count = desktop.factory().integer();
		final Pointer ptr = desktop.library().glfwGetJoystickButtons(id, count);
		return ptr.getByteArray(0, count.getValue());
	}

	/**
	 * @return Joystick buttons
	 */
	List<Button> buttons() {
		return Arrays.asList(buttons);
	}

//	/**
//	 * @return Joystick hats
//	 */
//	private Hat[] initHats() {
//		// Retrieve hat values
//		final byte[] values = getHatArray();
//
//		// Create hats
//		final Hat[] hats = IntStream
//				.range(0, values.length)
//				.mapToObj(id -> Button.name("Hat", id))
//				.map(Hat::new)
//				.toArray(Hat[]::new);
//
//		// Init hat values
//		init(hats);
//		update(values, hats);
//
//		return hats;
//	}
//
//	/**
//	 * Queries the hat values for this joystick.
//	 */
//	private byte[] getHatArray() {
//		final IntByReference count = desktop.factory().integer();
//		final Pointer ptr = desktop.library().glfwGetJoystickHats(id, count);
//		return ptr.getByteArray(0, count.getValue());
//	}
//
//	/**
//	 * @return Joystick buttons
//	 */
//	List<Hat> hats() {
//		return Arrays.asList(hats);
//	}

	/**
	 * Polls for joystick button events.
	 */
	void poll() {
//		// Ignore if no action handler
//		if(handler == null) {
//			return;
//		}

		// Poll events
		update(getButtonArray(), buttons);
//		update(getHatArray(), hats);
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

//		// Ignore if no event handler
//		if(handler == null) {
//			return;
//		}
//
//		// Generate event
//		final Button event = button.resolve(value);
//		handler.accept(event);
	}
}
