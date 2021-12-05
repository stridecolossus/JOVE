package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toList;

import java.awt.MouseInfo;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.PositionEvent;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

/**
 * The <i>mouse device</i> exposes event sources for the mouse pointer, buttons and scroll-wheel.
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
	public Set<Source<?>> sources() {
		return Set.of(ptr, buttons, wheel);
	}

	/**
	 * @return Mouse pointer
	 */
	public Source<PositionEvent> pointer() {
		return ptr;
	}

	/**
	 * @return Mouse buttons
	 */
	public Source<Button> buttons() {
		return buttons;
	}

	/**
	 * @return Mouse scroll-wheel axis
	 */
	public Axis wheel() {
		return wheel;
	}

	/**
	 * @return Number of mouse buttons
	 */
	public int count() {
		return buttons.buttons.size();
	}

	/**
	 * Mouse pointer event source.
	 */
	private class MousePointer extends DesktopSource<MousePositionListener, PositionEvent> {
		@Override
		protected MousePositionListener listener(Consumer<Event> handler) {
			return (ptr, x, y) -> {
				final PositionEvent pos = new PositionEvent(this, (float) x, (float) y);
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
	private class MouseButton extends DesktopSource<MouseButtonListener, Button> {
		@SuppressWarnings("hiding")
		private final List<ModifiedButton> buttons = IntStream
				.rangeClosed(1, MouseInfo.getNumberOfButtons())
				.mapToObj(id -> Button.name("Mouse", id))
				.map(ModifiedButton::new)
				.collect(toList());

		@Override
		protected MouseButtonListener listener(Consumer<Event> handler) {
			return (ptr, index, action, mods) -> {
				final ModifiedButton button = buttons.get(index);
				final Button event = button.resolve(action, mods);
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
	private class MouseWheel extends DesktopSource<MouseScrollListener, AxisEvent> implements Axis {
		@Override
		protected MouseScrollListener listener(Consumer<Event> handler) {
			return (ptr, x, y) -> {
				final AxisEvent e = new AxisEvent(this, (float) y);
				handler.accept(e);
			};
		}

		@Override
		protected BiConsumer<Window, MouseScrollListener> method(DesktopLibrary lib) {
			return lib::glfwSetScrollCallback;
		}
	}
}
