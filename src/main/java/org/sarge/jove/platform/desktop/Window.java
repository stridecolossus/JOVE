package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.TransientNativeObject;
import org.sarge.jove.control.Device;

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
	private final DesktopLibrary lib;
	private final WindowDescriptor props;

	/**
	 * Constructor.
	 * @param window		Window handle
	 * @param lib			GLFW API
	 * @param props			Window properties
	 */
	Window(Pointer window, DesktopLibrary lib, WindowDescriptor props) {
		this.handle = new Handle(window);
		this.lib = notNull(lib);
		this.props = notNull(props);
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

	/**
	 * @return GLFW
	 */
	DesktopLibrary library() {
		return lib;
	}

	/**
	 * @return New keyboard device
	 */
	public Device keyboard() {
		return new KeyboardDevice(this);
	}

	/**
	 * @return New mouse device
	 */
	public Device mouse() {
		return new MouseDevice(this);
	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param vulkan Vulkan instance handle
	 * @return Vulkan surface
	 */
	public Handle surface(Handle vulkan) {
		final PointerByReference ref = new PointerByReference();
		final int result = lib.glfwCreateWindowSurface(vulkan, handle, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	public void destroy() {
		lib.glfwDestroyWindow(handle);
	}
}
