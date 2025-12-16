package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;
import java.util.function.*;

import org.sarge.jove.control.AxisEvent;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

/**
 * The <i>mouse wheel</i> device generates scroll events.
 * @author Sarge
 */
public class MouseWheel extends AbstractWindowDevice<AxisEvent, MouseListener> {
	/**
	 * Constructor.
	 * @param window Parent window
	 */
	MouseWheel(Window window) {
		super(window);
	}

	@Override
	protected MouseListener callback(Window window, Consumer<AxisEvent> listener) {
		return new MouseListener() {
			@Override
			public void event(MemorySegment window, double x, double y) {
				listener.accept(new AxisEvent((int) y));
			}
		};
	}

	@Override
	protected BiConsumer<Window, MouseListener> method(DeviceLibrary library) {
		return library::glfwSetScrollCallback;
	}
}
