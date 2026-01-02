package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.function.*;

import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.KeyListener;

/**
 * The <i>keyboard device</i> generates keyboard button events.
 * @author Sarge
 */
public class Keyboard extends AbstractWindowDevice<ButtonEvent, KeyListener> {
	private final KeyTable table = KeyTable.Instance.INSTANCE.get();

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	Keyboard(Window window) {
		super(window);
	}

	@Override
	protected KeyListener callback(Window window, Consumer<ButtonEvent> listener) {
		return new KeyListener() {
			@Override
			public void key(MemorySegment window, int key, int scancode, int action, int mods) {
				// Lookup key
				final Button button = table.index().get(key);
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
