package org.sarge.jove.platform.glfw;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.jove.control.Event;
import org.sarge.jove.platform.Device;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.KeyListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MouseScrollListener;

import com.sun.jna.Pointer;

/**
 * GLFW window.
 * @author Sarge
 */
class FrameworkWindow extends Handle implements Window, Resource {
	private final FrameworkLibrary instance;
	private final Descriptor props;
	private final Device<?> device;

	/**
	 * Constructor.
	 * @param window		Window handle
	 * @param instance		GLFW API
	 * @param props			Window properties
	 */
	FrameworkWindow(Pointer window, FrameworkLibrary instance, Descriptor props) {
		super(window);
		this.instance = notNull(instance);
		this.props = notNull(props);
		this.device = createDevice(window, instance);
	}

	@Override
	public Descriptor descriptor() {
		return props;
	}

	@Override
	public Device<?> device() {
		return device;
	}

	@Override
	public void poll() {
		instance.glfwPollEvents();
	}

	/**
	 * Creates the device for this window.
	 * @param window		Window
	 * @param instance		GLFW instance
	 * @return New device
	 */
	private static Device<?> createDevice(Pointer window, FrameworkLibrary instance) {
		final Map<Event.Category, Device.Entry<Pointer, ?>> map = Map.of(
			Event.Category.BUTTON,	new Device.Entry<>(FrameworkWindow::key, instance::glfwSetKeyCallback),
			Event.Category.MOVE, 	new Device.Entry<>(FrameworkWindow::move, instance::glfwSetCursorPosCallback),
			Event.Category.CLICK, 	new Device.Entry<>(FrameworkWindow::button, instance::glfwSetMouseButtonCallback),
			Event.Category.ZOOM, 	new Device.Entry<>(FrameworkWindow::scroll, instance::glfwSetScrollCallback)
		);
		return new Device<>(window, map);
	}

	/**
	 * Creates a GLFW key listener.
	 * @param handler Event handler
	 * @return Key listener
	 */
	private static KeyListener key(Event.Handler handler) {
		return new KeyListener() {
			@Override
			public void key(Pointer window, int num, int scancode, int action, int mods) {
				final Event.Operation op = FrameworkHelper.operation(action);
				final Event.Descriptor descriptor = Event.Descriptor.of(Event.Category.BUTTON, num, op);
				final Event event = Event.of(descriptor);
				handler.handle(event);
			}
		};
	}

	/**
	 * Creates a GLFW mouse movement listener.
	 * @param handler Event handler
	 * @return Mouse movement listener
	 */
	private static MousePositionListener move(Event.Handler handler) {
		return (window, x, y) -> {
			final Event event = Event.of(Event.Descriptor.MOVE, (int) x, (int) y);
			handler.handle(event);
		};
	}

	/**
	 * Creates a GLFW mouse button listener.
	 * @param handler Event handler
	 * @return Mouse button listener
	 */
	private static MouseButtonListener button(Event.Handler handler) {
		return (window, button, action, mods) -> {
			final Event.Operation op = FrameworkHelper.operation(action);
			final Event.Descriptor descriptor = Event.Descriptor.of(Event.Category.CLICK, button, op);
			final Event event = Event.of(descriptor);
			handler.handle(event);
		};
	}

	/**
	 * Creates a GLFW mouse scroll listener.
	 * @param handler Event handler
	 * @return Mouse scroll listener
	 */
	private static MouseScrollListener scroll(Event.Handler handler) {
		return (window, x, y) -> {
			final Event event = Event.of(Event.Descriptor.ZOOM, (int) x, (int) y);
			handler.handle(event);
		};
	}

	@Override
	public void destroy() {
		instance.glfwDestroyWindow(super.handle());
	}
}