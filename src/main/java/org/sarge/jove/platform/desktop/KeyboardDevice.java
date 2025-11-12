package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

/**
 * The <i>keyboard device</i> generates GLFW keyboard events.
 * @author Sarge
 */
public class KeyboardDevice {
	private final Window window;
//	private final KeyboardSource keyboard = new KeyboardSource();
//	private final KeyTable table = KeyTable.INSTANCE;

	/**
	 * Constructor.
	 * @param window Window
	 */
	KeyboardDevice(Window window) {
		this.window = requireNonNull(window);
	}
}

//
//	@Override
//	public String name() {
//		return "Keyboard";
//	}
//
//	/**
//	 * @return Keyboard event source
//	 */
//	public Source<Button<Action>> keyboard() {
//		return keyboard;
//	}
//
//	@Override
//	public Set<Source<?>> sources() {
//		return Set.of(keyboard);
//	}
//
//	/**
//	 * Keyboard event source.
//	 */
//	private class KeyboardSource implements DesktopSource<KeyListener, Button<Action>> {
//		@Override
//		public String name() {
//			return "Keyboard";
//		}
//
//		@Override
//		public Window window() {
//			return window;
//		}
//
//		@Override
//		public KeyListener listener(Consumer<Button<Action>> handler) {
//			return (ptr, key, scancode, action, mods) -> {
//				final String name = table.name(key);
//				final Button<Action> button = new Button<>(name, Action.map(action));
//				// TODO - modifiers
//				handler.accept(button);
//			};
//		}
//
//		@Override
//		public BiConsumer<Window, KeyListener> method(DesktopLibrary lib) {
//			//return lib::glfwSetKeyCallback;
//			return null;
//		}
//	}
//}
