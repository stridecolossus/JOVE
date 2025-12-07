package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.function.*;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.KeyListener;

/**
 * The <i>keyboard device</i> generates keyboard button events.
 * @author Sarge
 */
public class KeyboardDevice extends AbstractWindowDevice<ButtonEvent, KeyListener> {
	private static final KeyTable KEYS = new KeyTable();

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	public KeyboardDevice(Window window) {
		super(window);
	}

	@Override
	protected KeyListener callback(Window window, Consumer<ButtonEvent> listener) {
		return new KeyListener() {
			@Override
			public void key(MemorySegment window, int key, int scancode, int action, int mods) {
				final Button button = new Button(key, KEYS.name(key));
				final var event = new ButtonEvent(
						KeyboardDevice.this,
						button,
						ButtonAction.map(action),
						ModifierKey.map(mods)
				);
				listener.accept(event);
			}
		};
	}

	@Override
	protected BiConsumer<Window, KeyListener> method(DeviceLibrary library) {
		return library::glfwSetKeyCallback;
	}
}
