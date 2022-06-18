package org.sarge.jove.platform.desktop;

import java.awt.MouseInfo;
import java.util.Set;
import java.util.function.*;
import java.util.stream.IntStream;

import org.sarge.jove.control.*;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.*;

/**
 * The <i>mouse device</i> exposes event sources for the mouse pointer, buttons and scroll-wheel.
 * @author Sarge
 */
public class MouseDevice extends DesktopDevice {
	private final MouseWheel wheel = new MouseWheel();
	private final MouseButtonSource buttons = new MouseButtonSource();
	private final MousePointer ptr = new MousePointer();

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	MouseDevice(Window window) {
		super(window);
	}

	@Override
	public String name() {
		return "Mouse";
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
	 * Mouse pointer event source.
	 */
	private class MousePointer extends DesktopSource<MouseListener, PositionEvent> {
		@Override
		public String name() {
			return "MousePointer";
		}

		@Override
		protected MouseListener listener(Consumer<PositionEvent> handler) {
			return (ptr, x, y) -> {
				final PositionEvent pos = new PositionEvent(this, (float) x, (float) y);
				handler.accept(pos);
			};
		}

		@Override
		protected BiConsumer<Window, MouseListener> method(DesktopLibrary lib) {
			return lib::glfwSetCursorPosCallback;
		}
	}

	/**
	 * Mouse buttons event source.
	 */
	private class MouseButtonSource extends DesktopSource<MouseButtonListener, Button> {
		private final String[] id = IntStream
				.rangeClosed(1, MouseInfo.getNumberOfButtons())
				.mapToObj(id -> Button.name("Mouse", id))
				.toArray(String[]::new);

		@Override
		public String name() {
			return "MouseButtons";
		}

		@Override
		protected MouseButtonListener listener(Consumer<Button> handler) {
			return (ptr, index, action, mods) -> {
				final Button button = new Button(MouseButtonSource.this, id[index], action, mods);
				handler.accept(button);
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
	private class MouseWheel extends DesktopSource<MouseListener, Axis> implements Axis {
		private float value;

		@Override
		public String name() {
			return "MouseWheel";
		}

		@Override
		public Source<?> source() {
			return this;
		}

		@Override
		public float value() {
			return value;
		}

		@Override
		protected MouseListener listener(Consumer<Axis> handler) {
			return (ptr, x, y) -> {
				this.value = (float) y;
				handler.accept(MouseWheel.this);
			};
		}

		@Override
		protected BiConsumer<Window, MouseListener> method(DesktopLibrary lib) {
			return lib::glfwSetScrollCallback;
		}
	}
}
