package org.sarge.jove.platform.desktop;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * The <i>keyboard device</i>
 * @author Sarge
 */
public class KeyboardDevice extends DesktopDevice {
	private static final Map<Integer, String> TABLE = new HashMap<>();

	static {
		try(final InputStream in = KeyboardDevice.class.getResourceAsStream("/key.table.txt")) {
			new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(StringUtils::split)
					.forEach(KeyboardDevice::load);
		}
		catch(Exception e) {
			throw new RuntimeException("Error loading key-table", e);
		}
	}

	private static void load(String[] tokens) {
		final Integer key = Integer.parseInt(tokens[1].trim());
		final String name = tokens[0].trim();
		TABLE.put(key, name);
	}

	private final KeyboardSource keyboard = new KeyboardSource();

	/**
	 * Constructor.
	 * @param window Window
	 */
	KeyboardDevice(Window window) {
		super(window);
	}

	/**
	 * @return Keyboard event source
	 */
	public DesktopSource<KeyListener> source() {
		return keyboard;
	}

	@Override
	public Set<Source> sources() {
		return Set.of(keyboard);
	}

	@Override
	public String toString() {
		return "Keyboard";
	}

	/**
	 * Keyboard event source.
	 */
	private class KeyboardSource extends DesktopSource<KeyListener> {
		@Override
		public List<Type<?>> types() {
			return List.of();
		}

		@Override
		protected KeyListener listener(Consumer<Event> handler) {
			return (ptr, key, scancode, action, mods) -> {
				// Lookup key
				final String name = TABLE.get(key);
				if(name == null) throw new RuntimeException("Unknown key code: " + key);

				// Create event
				// TODO - just inject mods/action into button ctor, no need to parse until name() is actually needed
				// - or maybe GLFW specific button class with mods/action?
				final String id = DesktopDevice.name(Event.name("Key", name), action, mods);
				final Button button = new Button(id, KeyboardSource.this);
				// TODO - cache?

				// Delegate
				handler.accept(button);
			};
		}

		@Override
		protected BiConsumer<Window, KeyListener> method(DesktopLibrary lib) {
			return lib::glfwSetKeyCallback;
		}
	}
}
