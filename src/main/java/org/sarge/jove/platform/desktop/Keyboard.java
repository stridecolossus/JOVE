package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.function.*;

import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.KeyListener;

/**
 * The <i>keyboard device</i> generates keyboard button events.
 * @author Sarge
 */
public class Keyboard extends AbstractWindowDevice<ButtonEvent, KeyListener> {
	private final Map<Integer, Button> keys;

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	Keyboard(Window window) {
		super(window);
		this.keys = KeyTable.Instance.INSTANCE.table().map(Button::new);
	}

	/**
	 * @return Keyboard keys
	 */
	public Map<Integer, Button> keys() {
		return keys;
	}

	@Override
	protected KeyListener callback(Window window, Consumer<ButtonEvent> listener) {
		return new KeyListener() {
			@Override
			public void key(MemorySegment window, int key, int scancode, int action, int mods) {
				// Lookup key
				final Button button = keys.get(key);
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
