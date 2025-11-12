package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

/**
 * The <i>mouse device</i> exposes event sources for the mouse pointer, buttons and scroll-wheel.
 * @author Sarge
 */
public class MouseDevice {
	private final Window window;
	private final DeviceLibrary library;

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	public MouseDevice(Window window) {
		this.window = requireNonNull(window);
		this.library = (DeviceLibrary) window.library();
	}

	public void poll() {
		library.glfwPollEvents();
	}

	public void add(MouseListener listener) {
		System.err.println("add listener");
		library.glfwSetCursorPosCallback(window, listener);
	}
}

//	private final MousePointer pointer = new MousePointer();
//	private final MouseButtonSource buttons = new MouseButtonSource();
//	private final MouseWheel wheel = new MouseWheel();
//
//	@Override
//	public String name() {
//		return "Mouse";
//	}
//
//	@Override
//	public Set<Source<?>> sources() {
//		return Set.of(pointer, buttons, wheel);
//	}
//
//	/**
//	 * @return Mouse pointer
//	 */
//	public Source<Position> pointer() {
//		return pointer;
//	}
//
//	/**
//	 * @return Mouse buttons
//	 */
//	public Source<Button<Action>> buttons() {
//		return buttons;
//	}
//
//	/**
//	 * @return Mouse scroll-wheel axis
//	 */
//	public AxisControl wheel() {
//		return wheel;
//	}
//
//	/**
//	 * Mouse pointer event source.
//	 */
//	private class MousePointer implements DesktopSource<MouseListener, Position> {
//		@Override
//		public String name() {
//			return "MousePointer";
//		}
//
//		@Override
//		public Window window() {
//			return window;
//		}
//
//		@Override
//		public MouseListener listener(Consumer<Position> handler) {
//			return (ptr, x, y) -> {
//				final Position pos = new Position((float) x, (float) y);
//				handler.accept(pos);
//			};
//		}
//
//		@Override
//		public BiConsumer<Window, MouseListener> method(DesktopLibrary lib) {
//			//return lib::glfwSetCursorPosCallback;
//			return null;
//		}
//	}
//
//	/**
//	 * Mouse buttons event source.
//	 */
//	private class MouseButtonSource implements DesktopSource<MouseButtonListener, Button<Action>> {
//		private final String[] id = IntStream
//				.rangeClosed(1, MouseInfo.getNumberOfButtons())
//				.mapToObj(id -> Event.name("Mouse", id))
//				.toArray(String[]::new);
//
//		@Override
//		public String name() {
//			return "MouseButtons";
//		}
//
//		@Override
//		public Window window() {
//			return window;
//		}
//
//		@Override
//		public MouseButtonListener listener(Consumer<Button<Action>> handler) {
//			return (ptr, index, action, mods) -> {
//				final Button<Action> button = new Button<>(id[index], Action.map(action));
//				// TODO - modifiers
//				handler.accept(button);
//			};
//		}
//
//		@Override
//		public BiConsumer<Window, MouseButtonListener> method(DesktopLibrary lib) {
//			return null;
////			return lib::glfwSetMouseButtonCallback;
//		}
//	}
//
//	/**
//	 * Mouse scroll-wheel source.
//	 */
//	private class MouseWheel extends AxisControl implements DesktopSource<MouseListener, AxisControl> {
//		@Override
//		public String name() {
//			return "MouseWheel";
//		}
//
//		@Override
//		public Window window() {
//			return window;
//		}
//
//		@Override
//		public MouseListener listener(Consumer<AxisControl> handler) {
//			return (ptr, x, y) -> {
//				update((float) y);
//				handler.accept(MouseWheel.this);
//			};
//		}
//
//		@Override
//		public BiConsumer<Window, MouseListener> method(DesktopLibrary lib) {
////			return lib::glfwSetScrollCallback;
//			return null;
//		}
//	}
//}
