package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.awt.MouseInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.InputEvent.Device;
import org.sarge.jove.control.InputEvent.Source;
import org.sarge.jove.control.Position;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

/**
 * A <i>mouse device</i> generates events for a mouse controller.
 * TODO - doc
 * @author Sarge
 */
public class MouseDevice implements Device {
	/**
	 * Mouse pointer.
	 */
	private class Pointer implements Source<Position> {
		@Override
		public List<Position> events() {
			return List.of(Position.TYPE);
		}

		@Override
		public void enable(Consumer<InputEvent<Position>> handler) {
			final MousePositionListener listener = (ptr, x, y) -> handler.accept(new Position.Event((float) x, (float) y));
			apply(listener);
		}

		@Override
		public void disable() {
			apply(null);
		}

		private void apply(MousePositionListener listener) {
			window.library().glfwSetCursorPosCallback(window.handle(), listener);
		}
	}

	/**
	 * Mouse buttons.
	 */
	private class MouseButtons implements Source<Button> {
		/**
		 * @return Number of mouse buttons
		 */
		private int count() {
			// TODO - uses AWT but not supported by GLFW
			return MouseInfo.getNumberOfButtons();
		}

		private final Button[] buttons = IntStream.rangeClosed(1, count()).mapToObj(n -> "Button-" + n).map(Button::new).toArray(Button[]::new);

		@Override
		public List<Button> events() {
			return Arrays.asList(buttons);
		}

		@Override
		public void enable(Consumer<InputEvent<Button>> handler) {
			final MouseButtonListener listener = (ptr, button, action, mods) -> {
				// TODO - action/mods
				handler.accept(buttons[button]);
			};
			apply(listener);
		}

		@Override
		public void disable() {
			apply(null);
		}

		private void apply(MouseButtonListener listener) {
			window.library().glfwSetMouseButtonCallback(window.handle(), listener);
		}
	}

	/**
	 * Mouse wheel.
	 */
	private class Wheel implements Source<Axis> {
		private final Axis wheel = new Axis("Wheel");

		@Override
		public List<Axis> events() {
			return List.of(wheel);
		}

		@Override
		public void enable(Consumer<InputEvent<Axis>> handler) {
			final MouseScrollListener listener = (ptr, x, y) -> handler.accept(wheel.create((float) y));
			apply(listener);
		}

		@Override
		public void disable() {
			apply(null);
		}

		private void apply(MouseScrollListener listener) {
			window.library().glfwSetScrollCallback(window.handle(), listener);
		}
	}

	private final Window window;

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	MouseDevice(Window window) {
		this.window = notNull(window);
	}

	@Override
	public String name() {
		return "Mouse";
	}

	/**
	 * @return Mouse pointer
	 */
	public Source<Position> pointer() {
		return new Pointer();
	}

	/**
	 * @return Mouse buttons
	 */
	public Source<Button> buttons() {
		return new MouseButtons();
	}

	/**
	 * @return Mouse wheel
	 */
	public Source<Axis> wheel() {
		return new Wheel();
	}

	@Override
	public Set<Source<?>> sources() {
		return Set.of(pointer(), buttons(), wheel());
	}
}
