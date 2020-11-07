package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.InputEvent.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * A <i>keyboard device</i> generates {@link Button} events.
 * @see KeyTable
 * @author Sarge
 */
public class KeyboardDevice implements InputEvent.Device {
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
	public Set<Source<?>> sources() {
		return Set.of(keyboard());
	}

	/**
	 * Helper - Enables this keyboard for the given event handler.
	 * @param handler Event handler
	 */
	public void enable(Consumer<Button> handler) {
		final var keyboard = keyboard();
		keyboard.enable(handler);
	}

	/**
	 * @return New keyboard event source
	 */
	private Source<Button> keyboard() {
		return new Source<>() {
			@Override
			public List<Button> types() {
				return List.of();
			}

			@Override
			public void enable(Consumer<Button> handler) {
				// Create callback adapter
				final KeyListener listener = (ptr, key, scancode, action, mods) -> {
					final String name = KeyTable.INSTANCE.map(key);
					final Button button = new Button(name, action, mods);
					handler.accept(button);
				};

				// Register callback
				apply(listener);
			}

			@Override
			public void disable() {
				apply(null);
			}

			/**
			 * Sets the GLFW keyboard listener.
			 * @param listener Keyboard listener
			 */
			private void apply(KeyListener listener) {
				window.library().glfwSetKeyCallback(window.handle(), listener);
			}
		};
	}

	/**
	 * The <i>key table</i> maps between GLFW key codes and names.
	 */
	public static class KeyTable {
		/**
		 * Singleton instance.
		 */
		public static final KeyTable INSTANCE = new KeyTable();

		private final BidiMap<Integer, String> table = new DualHashBidiMap<>(load());

		private KeyTable() {
		}

		/**
		 * Helper - Maps a key code to name.
		 */
		public String map(int code) {
			final String name = table.get(code);
			if(name == null) throw new IllegalArgumentException("Unknown key code: " + code);
			return name;
		}

		/**
		 * Loads the standard key table.
		 */
		private static Map<Integer, String> load() {
			try(final InputStream in = KeyTable.class.getResourceAsStream("/key.table.txt")) {
				if(in == null) throw new RuntimeException("Cannot find key names resource");
				return new BufferedReader(new InputStreamReader(in))
						.lines()
						.map(StringUtils::split)
						.collect(toMap(tokens -> Integer.parseInt(tokens[1].trim()), tokens -> tokens[0].trim()));
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
