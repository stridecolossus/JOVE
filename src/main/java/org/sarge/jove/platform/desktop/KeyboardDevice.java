package org.sarge.jove.platform.desktop;

import java.util.Set;
import java.util.function.*;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * The <i>keyboard device</i> generates GLFW keyboard events.
 * @author Sarge
 */
public class KeyboardDevice extends DesktopDevice {
	private final KeyboardSource keyboard = new KeyboardSource();
	private final KeyTable table = KeyTable.instance();

	/**
	 * Constructor.
	 * @param window Window
	 */
	KeyboardDevice(Window window) {
		super(window);
	}

	@Override
	public String name() {
		return "Keyboard";
	}

	/**
	 * @return Keyboard event source
	 */
	public Source<Button> keyboard() {
		return keyboard;
	}

	@Override
	public Set<Source<?>> sources() {
		return Set.of(keyboard);
	}

//	/**
//	 * Helper - Looks up a keyboard button by name.
//	 * @param name Key name
//	 * @return Keyboard button
//	 * @throws IllegalArgumentException for an unknown key name
//	 */
//	public Button key(String name) {
//		final int code = table.code(name);
//		return keyboard.key(code);
//	}

//	/**
//	 * Helper - Binds the keyboard to the given handler.
//	 * @param handler Keyboard handler
//	 */
//	public void bind(Consumer<Button> handler) {
//		keyboard.bind(handler);
//	}

	/**
	 * Keyboard event source.
	 */
	private class KeyboardSource extends DesktopSource<KeyListener, Button> {
		@Override
		public String name() {
			return "Keyboard";
		}

		@Override
		protected KeyListener listener(Consumer<Button> handler) {
			return (ptr, key, scancode, action, mods) -> {
				final String name = table.name(key);
				final Button button = new Button(KeyboardSource.this, name, action, mods);
				handler.accept(button);
			};
		}

//		private ModifiedButton key(int code) {
//			return new ModifiedButton(table.name(code));
//		}

		@Override
		protected BiConsumer<Window, KeyListener> method(DesktopLibrary lib) {
			return lib::glfwSetKeyCallback;
		}
	}
}
