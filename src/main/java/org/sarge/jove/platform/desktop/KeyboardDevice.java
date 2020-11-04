package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Operation;
import org.sarge.jove.control.Device;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * A <i>keyboard device</i> generates {@link Button} events.
 * @see KeyTable
 * @author Sarge
 */
public class KeyboardDevice implements Device {
	private final KeyTable table = new KeyTable();
	private final Map<Integer, Button> buttons = new HashMap<>();
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
	public void enable(Class<? extends Type> type, Consumer<InputEvent<?>> handler) {
		// Create callback adapter
		final KeyListener listener = (ptr, key, scancode, action, mods) -> {
			// TODO - action/mods
			final Button button = key(key);
			handler.accept(button.event(Operation.PRESS));
		};

		// Register callback
		apply(type, listener);
	}

	private void apply(Class<? extends Type> type, KeyListener listener) {
		if(type != Button.class) throw new IllegalArgumentException("Invalid event type for keyboard: " + type);
		window.library().glfwSetKeyCallback(window.handle(), listener);
	}

	/**
	 * Looks up the key for the given key-code.
	 * @param code Key-code
	 * @return Key
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public Button key(int code) {
		return buttons.computeIfAbsent(code, ignored -> new Button(table.name(code)));
	}

	/**
	 * Looks up a key by name.
	 * @param name Key name
	 * @return Key
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public Button key(String name) {
		final int code = table.code(name);
		return buttons.computeIfAbsent(code, ignored -> new Button(name));
	}
}
