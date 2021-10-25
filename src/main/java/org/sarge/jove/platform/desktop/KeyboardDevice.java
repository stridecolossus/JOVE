package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.ButtonEvent;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * The <i>keyboard device</i>
 * @author Sarge
 */
public class KeyboardDevice extends DesktopDevice {
	private static final Map<Integer, Type> TABLE;

	static {
		try(final InputStream in = KeyboardDevice.class.getResourceAsStream("/key.table.txt")) {
			TABLE = new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(StringUtils::split)
					.map(KeyboardDevice::load)
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		catch(Exception e) {
			throw new RuntimeException("Error loading key-table", e);
		}
	}

	private static Map.Entry<Integer, Type> load(String[] tokens) {
		final Integer key = Integer.parseInt(tokens[1].trim());
		final Type type = new Type(tokens[0].trim());
		return Map.entry(key, type);
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
	public DesktopSource<KeyListener> keyboard() {
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
		public Collection<Type> types() {
			return List.of();
		}

		@Override
		protected KeyListener listener(Consumer<Event> handler) {
			return (ptr, key, scancode, action, mods) -> {
				// Lookup key
				final Type type = TABLE.get(key);
				if(type == null) throw new RuntimeException("Unknown key code: " + key);

				// Create event
				final String prefix = Event.name("Key", type.name());
				final String name = DesktopDevice.name(prefix, action, mods);
				final ButtonEvent button = new ButtonEvent(name, type, KeyboardSource.this);

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
