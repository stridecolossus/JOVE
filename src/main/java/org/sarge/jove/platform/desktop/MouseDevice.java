package org.sarge.jove.platform.desktop;

import java.awt.MouseInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.AxisEvent;
import org.sarge.jove.control.ButtonEvent;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.control.PositionEvent;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

/**
 * The <i>mouse device</i> comprises event sources for the mouse pointer, buttons and scroll-wheel.
 * @author Sarge
 */
public class MouseDevice extends DesktopDevice {
	private final MouseWheel wheel = new MouseWheel();
	private final MouseButton buttons = new MouseButton();
	private final MousePointer ptr = new MousePointer();

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	MouseDevice(Window window) {
		super(window);
	}

	@Override
	public Set<Source> sources() {
		return Set.of(ptr, buttons, wheel);
	}

	/**
	 * @return Mouse pointer
	 */
	public DesktopSource<MousePositionListener> pointer() {
		return ptr;
	}

	/**
	 * @return Mouse buttons
	 */
	public DesktopSource<MouseButtonListener> buttons() {
		return buttons;
	}

	/**
	 * @return Mouse scroll-wheel
	 */
	public DesktopSource<MouseScrollListener> wheel() {
		return wheel;
	}

	@Override
	public String toString() {
		return "MouseDevice";
	}

	/**
	 * Mouse pointer event source.
	 */
	private class MousePointer extends DesktopSource<MousePositionListener> {
		private final Type pointer = new Type("Pointer");

		@Override
		public Collection<Type> types() {
			return Set.of(pointer);
		}

		@Override
		protected MousePositionListener listener(Consumer<Event> handler) {
			return (ptr, x, y) -> {
				final PositionEvent pos = new PositionEvent(pointer, MousePointer.this, (float) x, (float) y);
				handler.accept(pos);
			};
		}

		@Override
		protected BiConsumer<Window, MousePositionListener> method(DesktopLibrary lib) {
			return lib::glfwSetCursorPosCallback;
		}
	}

	/**
	 * Mouse buttons event source.
	 */
	private class MouseButton extends DesktopSource<MouseButtonListener> {
		private final String prefix = Event.name("Mouse", "Button", StringUtils.EMPTY);

		private final Type[] types = IntStream
				.rangeClosed(1, MouseInfo.getNumberOfButtons())
				.mapToObj(String::valueOf)
				.map(prefix::concat)
				.map(Type::new)
				.toArray(Type[]::new);

		@Override
		public Collection<Type> types() {
			return Arrays.asList(types);
		}

		@Override
		protected MouseButtonListener listener(Consumer<Event> handler) {
			return (ptr, index, action, mods) -> {
				final Type button = types[index];
				final String name = DesktopDevice.name(button.name(), action, mods);
				final ButtonEvent event = new ButtonEvent(name, button, MouseButton.this);
				handler.accept(event);
			};
		}

		@Override
		protected BiConsumer<Window, MouseButtonListener> method(DesktopLibrary lib) {
			return lib::glfwSetMouseButtonCallback;
		}
	}

	/**
	 * Mouse scroll-wheel source.
	 */
	private class MouseWheel extends DesktopSource<MouseScrollListener> {
		private final Type axis = new Type("Wheel");

		@Override
		public Set<Type> types() {
			return Set.of(axis);
		}

		@Override
		protected MouseScrollListener listener(Consumer<Event> handler) {
			return (ptr, x, y) -> {
				final AxisEvent event = new AxisEvent(axis, MouseWheel.this, (float) y);
				handler.accept(event);
			};
		}

		@Override
		protected BiConsumer<Window, MouseScrollListener> method(DesktopLibrary lib) {
			return lib::glfwSetScrollCallback;
		}
	}
}
