package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.control.Device;
import org.sarge.jove.control.InputEvent.Handler;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Button;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.util.Check;

/**
 * A <i>keyboard device</i> generates {@link Button} events.
 * @author Sarge
 */
class KeyboardDevice implements Device {
	private final Window window;

	/**
	 * Constructor.
	 * @param window Window
	 */
	KeyboardDevice(Window window) {
		this.window = notNull(window);
	}

	@Override
	public String name() {
		return "Keyboard";
	}

	@Override
	public Set<Class<? extends Type>> types() {
		return Set.of(Button.class);
	}

	@Override
	public void enable(Class<? extends Type> type, Handler handler) {
		Check.notNull(handler);

		// Create callback adapter
		final KeyListener listener = (ptr, key, scancode, action, mods) -> {
			final String name = window.library().glfwGetKeyName(key, scancode); // TODO - need KeyName thingy?
			final Button button = new Button(key, name, action, mods);
			handler.handle(button.event());
		};

		// Register callback
		apply(type, listener);
	}

	@Override
	public void disable(Class<? extends Type> type) {
		apply(type, null);
	}

	private void apply(Class<? extends Type> type, KeyListener listener) {
		if(type != Button.class) throw new IllegalArgumentException("Invalid event type for keyboard: " + type);
		window.library().glfwSetKeyCallback(window.handle(), listener);
	}
}
