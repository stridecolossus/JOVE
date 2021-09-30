package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.function.Consumer;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * The <i>desktop</i> provides services for managing windows and monitors implemented using GLFW.
 * @see <a href="https://www.glfw.org/docs/latest/index.html">GLFW documentation</a>
 * @see <a href="https://github.com/badlogic/jglfw/blob/master/jglfw/jni/glfw-3.0/include/GL/glfw3.h">C header</a>
 * @author Sarge
 */
public class Desktop {
	/**
	 * Handler thats dumps errors to the console.
	 * @see #setErrorHandler(Consumer)
	 */
	public static final Consumer<String> CONSOLE_ERROR_HANDLER = System.err::println;

	/**
	 * Creates the desktop service.
	 * @return New desktop service
	 * @throws RuntimeException if GLFW cannot be initialised
	 * @throws UnsupportedOperationException if the GLFW cannot be found
	 * @throws UnsatisfiedLinkError if the native library cannot be instantiated
	 */
	public static Desktop create() {
		// Determine library name
		final String name = switch(Platform.getOSType()) {
			case Platform.WINDOWS -> "C:/GLFW/lib-mingw-w64/glfw3.dll";		// <--- TODO
			case Platform.LINUX -> "libglfw";
			default -> throw new UnsupportedOperationException("Unsupported platform for GLFW: " + Platform.getOSType());
		};

		// Init type mapper
		final var mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);

		// Load native library
		final DesktopLibrary lib = Native.load(name, DesktopLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, mapper));

		// Init GLFW
		final int result = lib.glfwInit();
		if(result != 1) throw new RuntimeException("Cannot initialise GLFW: code=" + result);

		// Create desktop
		return new Desktop(lib);
	}

	private final DesktopLibrary lib;

	/**
	 * Constructor.
	 * @param lib GLFW library
	 */
	Desktop(DesktopLibrary lib) {
		this.lib = notNull(lib);
	}

	/**
	 * @return GLFW library
	 */
	DesktopLibrary library() {
		return lib;
	}

	/**
	 * @return GLFW version
	 */
	public String version() {
		return lib.glfwGetVersionString();
	}

	/**
	 * @return Whether Vulkan is supported on the current hardware
	 */
	public boolean isVulkanSupported() {
		return lib.glfwVulkanSupported();
	}

	/**
	 * Processes pending input events.
	 */
	public void poll() {
		lib.glfwPollEvents();
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public String[] extensions() {
		final IntByReference size = new IntByReference();
		final Pointer ptr = lib.glfwGetRequiredInstanceExtensions(size);
		return ptr.getStringArray(0, size.getValue());
	}

	/**
	 * Sets a handler for GLFW errors.
	 * @param handler Error handler
	 * @see #CONSOLE_ERROR_HANDLER
	 */
	public void setErrorHandler(Consumer<String> handler) {
		final ErrorCallback callback = (error, description) -> {
			final String message = String.format("GLFW error: [%d] %s", error, description);
			handler.accept(message);
		};
		lib.glfwSetErrorCallback(callback);
	}

//	/**
//	 * @return Monitors on this desktop
//	 */
//	public List<Monitor> monitors() {
//		final IntByReference count = new IntByReference();
//		final Pointer[] monitors = instance.glfwGetMonitors(count).getPointerArray(0, count.getValue());
//		return Arrays.stream(monitors).map(this::monitor).collect(toList());
//	}
//
//	/**
//	 * Retrieves and maps a monitor descriptor.
//	 */
//	private Monitor monitor(Pointer handle) {
//		// Lookup monitor dimensions
//		final IntByReference w = new IntByReference();
//		final IntByReference h = new IntByReference();
//		instance.glfwGetMonitorPhysicalSize(handle, w, h);
//
//		// Lookup display modes
//		final IntByReference count = new IntByReference();
//		final FrameworkDisplayMode result = instance.glfwGetVideoModes(handle, count);
//		final FrameworkDisplayMode[] array = (FrameworkDisplayMode[]) result.toArray(count.getValue());
//		final List<Monitor.DisplayMode> modes = Arrays.stream(array).map(Desktop::toDisplayMode).collect(toList());
//
//		// Create monitor
//		final String name = instance.glfwGetMonitorName(handle);
////		return new Monitor(handle, name, new Dimensions(w.getValue(), h.getValue()), modes);
//		return null;
//	}
//
//	public DisplayMode mode(Monitor monitor) {
////		final FrameworkDisplayMode mode = instance.glfwGetVideoMode((Pointer) monitor.handle());
////		return toDisplayMode(mode);
//		return null;
//	}
//
//	/**
//	 * Maps a GLFW display mode.
//	 */
//	private static DisplayMode toDisplayMode(FrameworkDisplayMode mode) {
//		final int[] depth = new int[]{mode.red, mode.green, mode.blue};
//		return new Monitor.DisplayMode(new Dimensions(mode.width, mode.height), depth, mode.refresh);
//	}



//	/**
//	 * Creates a GLFW window.
//	 * @param lib				GLFW library
//	 * @param descriptor		Window descriptor
//	 * @param monitor			Optional monitor
//	 * @return New window
//	 * @throws RuntimeException if the window cannot be created
//	 */
//	public static Window create(DesktopLibrary lib, Descriptor descriptor, Monitor monitor) {
//		// TODO
//		if(monitor != null) throw new UnsupportedOperationException();
//
//		// Apply window hints
//		lib.glfwDefaultWindowHints();
//		descriptor.properties().forEach(p -> p.apply(lib));
//
//		// Create window
//		final Dimensions size = descriptor.size();
//		final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);	// TODO - monitor
//		if(window == null) {
//			throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
//		}
//
//		// Create window wrapper
//		return new Window(window, lib, descriptor);
//	}

	/**
	 * Creates a desktop window.
	 * @param descriptor Window descriptor
	 * @return New window
	 */
	public Window window(Window.Descriptor descriptor, Monitor monitor) {
		// Apply window hints
		lib.glfwDefaultWindowHints();
		descriptor.properties().forEach(p -> p.apply(lib));

		// Create window
		final Dimensions size = descriptor.size();
		final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);	// TODO - monitor
		if(window == null) {
			throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
		}

		// Create window wrapper
		return new Window(this, window, descriptor);
	}

	/**
	 * Destroys this desktop.
	 */
	public void destroy() {
		lib.glfwTerminate();
	}
}
