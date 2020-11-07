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
	public Source<Position.Event> pointer() {
		return new Source<>() {
			private final Position pos = new Position("Pointer");

			@Override
			public List<Position> types() {
				return List.of(pos);
			}

			@Override
			public void enable(Consumer<Position.Event> handler) {
				final MousePositionListener listener = (ptr, x, y) -> handler.accept(new Position.Event(pos, (float) x, (float) y));
				apply(listener);
			}

			@Override
			public void disable() {
				apply(null);
			}

			private void apply(MousePositionListener listener) {
				window.library().glfwSetCursorPosCallback(window.handle(), listener);
			}
		};
	}

	/**
	 * @return Mouse buttons
	 */
	public Source<Button> buttons() {
		return new Source<>() {
			/**
			 * @return Number of mouse buttons
			 */
			private int count() {
				// TODO - uses AWT but not supported by GLFW
				return MouseInfo.getNumberOfButtons();
			}

			private final Button[] buttons = IntStream.rangeClosed(1, count()).mapToObj(n -> "Button-" + n).map(Button::of).toArray(Button[]::new);

			@Override
			public List<Button> types() {
				return Arrays.asList(buttons);
			}

			@Override
			public void enable(Consumer<Button> handler) {
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
		};
	}

	/**
	 * @return Mouse wheel
	 */
	public Source<Axis.Event> wheel() {
		return new Source<>() {
			private final Axis wheel = new Axis("Wheel");

			@Override
			public List<Axis> types() {
				return List.of(wheel);
			}

			@Override
			public void enable(Consumer<Axis.Event> handler) {
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
		};
	}

	@Override
	public Set<Source<?>> sources() {
		return Set.of(pointer(), buttons(), wheel());
	}
}
