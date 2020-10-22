package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.TransientNativeObject;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window implements TransientNativeObject {
	/**
	 * Creates a GLFW window.
	 * @param lib				GLFW library
	 * @param descriptor		Window descriptor
	 * @param monitor			Optional monitor
	 * @return New window
	 * @throws RuntimeException if the window cannot be created
	 */
	static Window create(DesktopLibrary lib, WindowDescriptor descriptor, Monitor monitor) {
		// Apply window hints
		lib.glfwDefaultWindowHints();
		descriptor.properties().forEach(p -> p.apply(lib));

		// Create window
		final Dimensions size = descriptor.size();
		final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null/*monitor.handle()*/, null);
		if(window == null) {
			throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
		}

		// Create window wrapper
		return new Window(window, lib, descriptor);
	}

	private final Handle handle;
	private final DesktopLibrary instance;
	private final WindowDescriptor props;
//	private final Device<?> device;

	/**
	 * Constructor.
	 * @param window		Window handle
	 * @param lib			GLFW API
	 * @param props			Window properties
	 */
	Window(Pointer window, DesktopLibrary lib, WindowDescriptor props) {
		this.handle = new Handle(window);
		this.instance = notNull(lib);
		this.props = notNull(props);
//		this.device = null; // TODO - createDevice(window, instance);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Descriptor for this window
	 */
	public WindowDescriptor descriptor() {
		return props;
	}

	// TODO
	public void setMouseMoveListener(MousePositionListener listener) {
		//instance.glfwSetCursorPosCallback(ptr, listener);
	}
	public void setKeyListener(KeyListener listener) {
		//instance.glfwSetKeyCallback(ptr, listener);
	}

//	public Device<?> device() {
//		return null;
////		return device;
//	}

	// TODO - to device
	public void poll() {
		instance.glfwPollEvents();
	}

//	/**
//	 * Creates the device for this window.
//	 * @param window		Window
//	 * @param instance		GLFW instance
//	 * @return New device
//	 */
//	private static Device<?> createDevice(Pointer window, FrameworkLibrary instance) {
//		final Map<Event.Category, Device.Entry<Pointer, ?>> map = Map.of(
//			Event.Category.BUTTON,	new Device.Entry<>(FrameworkWindow::key, instance::glfwSetKeyCallback),
//			Event.Category.MOVE, 	new Device.Entry<>(FrameworkWindow::move, instance::glfwSetCursorPosCallback),
//			Event.Category.CLICK, 	new Device.Entry<>(FrameworkWindow::button, instance::glfwSetMouseButtonCallback),
//			Event.Category.ZOOM, 	new Device.Entry<>(FrameworkWindow::scroll, instance::glfwSetScrollCallback)
//		);
//		return new Device<>(window, map);
//	}
//
//	/**
//	 * Creates a GLFW key listener.
//	 * @param handler Event handler
//	 * @return Key listener
//	 */
//	private static KeyListener key(Event.Handler handler) {
//		return new KeyListener() {
//			@Override
//			public void key(Pointer window, int num, int scancode, int action, int mods) {
//				final Event.Operation op = FrameworkHelper.operation(action);
//				final Event.Descriptor descriptor = new Event.Descriptor(Event.Category.BUTTON, num, op);
//				final Event event = new Event(descriptor);
//				handler.handle(event);
//			}
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse movement listener.
//	 * @param handler Event handler
//	 * @return Mouse movement listener
//	 */
//	private static MousePositionListener move(Event.Handler handler) {
//		return (window, x, y) -> {
//			final Event event = new Event(Event.Descriptor.MOVE, (int) x, (int) y);
//			handler.handle(event);
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse button listener.
//	 * @param handler Event handler
//	 * @return Mouse button listener
//	 */
//	private static MouseButtonListener button(Event.Handler handler) {
//		return (window, button, action, mods) -> {
//			final Event.Operation op = FrameworkHelper.operation(action);
//			final Event.Descriptor descriptor = new Event.Descriptor(Event.Category.CLICK, button, op);
//			final Event event = new Event(descriptor);
//			handler.handle(event);
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse scroll listener.
//	 * @param handler Event handler
//	 * @return Mouse scroll listener
//	 */
//	private static MouseScrollListener scroll(Event.Handler handler) {
//		return (window, x, y) -> {
//			final Event event = new Event(Event.Descriptor.ZOOM, (int) x, (int) y);
//			handler.handle(event);
//		};
//	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param vulkan Vulkan instance handle
	 * @return Vulkan surface
	 */
	public Handle surface(Handle vulkan) {
		final PointerByReference ref = new PointerByReference();
		final int result = instance.glfwCreateWindowSurface(vulkan, handle, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	public void destroy() {
		instance.glfwDestroyWindow(handle);
	}
}
