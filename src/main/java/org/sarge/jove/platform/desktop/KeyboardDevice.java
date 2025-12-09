package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.function.*;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.KeyListener;

/**
 * The <i>keyboard device</i> generates keyboard button events.
 * @author Sarge
 */
public class KeyboardDevice extends AbstractWindowDevice<ButtonEvent, KeyListener> {
	private final Map<Integer, Button> buttons;

	/**
	 * Constructor using the default key table.
	 * @param window Parent window
	 * @see KeyTable#defaultKeyTable()
	 */
	public KeyboardDevice(Window window) {
		this(window, KeyTable.defaultKeyTable());
	}

	/**
	 * Constructor.
	 * @param window	Parent window
	 * @param table		Key table
	 */
	public KeyboardDevice(Window window, KeyTable table) {
		super(window);
		this.buttons = table.map(Button::new);
	}

	@Override
	protected KeyListener callback(Window window, Consumer<ButtonEvent> listener) {
		return new KeyListener() {
			@Override
			public void key(MemorySegment window, int key, int scancode, int action, int mods) {
				// Lookup key
				final Button button = buttons.get(key);
				if(button == null) {
					throw new RuntimeException("Unknown keyboard key: " + key);
				}

				// Create event
				final var event = new ButtonEvent(
						button,
						ButtonAction.map(action),
						ModifierKey.map(mods)
				);

				// Delegate to listener
				listener.accept(event);
			}
		};
	}

	@Override
	protected BiConsumer<Window, KeyListener> method(DeviceLibrary library) {
		return library::glfwSetKeyCallback;
	}
}
