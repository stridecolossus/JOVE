package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.function.*;

import org.sarge.jove.control.ScreenCoordinate;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

/**
 * The <i>mouse pointer</i> device generates mouse movement events.
 * @author Sarge
 */
public class MousePointer extends AbstractWindowDevice<ScreenCoordinate, MouseListener> {
	/**
	 * Constructor.
	 * @param window Parent window
	 */
	public MousePointer(Window window) {
		super(window);
	}

	@Override
	protected MouseListener callback(Window window, Consumer<ScreenCoordinate> listener) {
		return new MouseListener() {
			@Override
			public void event(MemorySegment window, double x, double y) {
				final var pos = new ScreenCoordinate((int) x, (int) y);
				listener.accept(pos);
			}
		};
	}

	@Override
	protected BiConsumer<Window, MouseListener> method(DeviceLibrary library) {
		return library::glfwSetCursorPosCallback;
	}
}
