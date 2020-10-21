package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.FrameworkDisplayMode;
import org.sarge.jove.platform.desktop.Monitor.DisplayMode;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Desktop service implemented using GLFW.
 * @author Sarge
 * @see <a href="https://www.glfw.org/docs/latest/index.html">GLFW documentation</a>
 * @see <a href="https://github.com/badlogic/jglfw/blob/master/jglfw/jni/glfw-3.0/include/GL/glfw3.h">C header</a>
 */
public class Desktop {
	/**
	 * Creates the GLFW desktop service.
	 * @return New desktop service
	 * @throws ServiceException if GLFW cannot be initialised
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

	private final DesktopLibrary instance;

	/**
	 * Constructor.
	 * @param instance GLFW instance
	 */
	Desktop(DesktopLibrary instance) {
		this.instance = notNull(instance);
	}

	// TODO
	/*
	public void handler(ErrorHandler handler) {
		final ErrorCallback callback = (error, description) -> {
			final String message = String.format("GLFW error: code=%d [%s]", error, description);
			handler.handle(message);
		};
		instance.glfwSetErrorCallback(callback);
	}
	*/

	public String version() {
		return instance.glfwGetVersionString();
	}

	public boolean isVulkanSupported() {
		return instance.glfwVulkanSupported();
	}

	/**
	 * Looks up the localised name of the given key.
	 * @param key
	 * @param scancode
	 * @return Key name
	 *
	 * TODO - this only works for the printable keys, e.g. not ESCAPE
	 * TODO - promote to DesktopService
	 *
	 */
	public String keyname(int key, int scancode) {
		return instance.glfwGetKeyName(key, scancode);
	}

	public String[] extensions() {
		final IntByReference size = new IntByReference();
		final PointerByReference extensions = instance.glfwGetRequiredInstanceExtensions(size);
		return extensions.getPointer().getStringArray(0, size.getValue());
	}

	public List<Monitor> monitors() {
		final IntByReference count = new IntByReference();
		final Pointer[] monitors = instance.glfwGetMonitors(count).getPointerArray(0, count.getValue());
		return Arrays.stream(monitors).map(this::monitor).collect(toList());
	}

	/**
	 * Retrieves and maps a monitor descriptor.
	 */
	private Monitor monitor(Pointer handle) {
		// Lookup monitor dimensions
		final IntByReference w = new IntByReference();
		final IntByReference h = new IntByReference();
		instance.glfwGetMonitorPhysicalSize(handle, w, h);

		// Lookup display modes
		final IntByReference count = new IntByReference();
		final FrameworkDisplayMode result = instance.glfwGetVideoModes(handle, count);
		final FrameworkDisplayMode[] array = (FrameworkDisplayMode[]) result.toArray(count.getValue());
		final List<Monitor.DisplayMode> modes = Arrays.stream(array).map(Desktop::toDisplayMode).collect(toList());

		// Create monitor
		final String name = instance.glfwGetMonitorName(handle);
		return new Monitor(handle, name, new Dimensions(w.getValue(), h.getValue()), modes);
	}

	public DisplayMode mode(Monitor monitor) {
		final FrameworkDisplayMode mode = instance.glfwGetVideoMode((Pointer) monitor.handle());
		return toDisplayMode(mode);
	}

	/**
	 * Maps a GLFW display mode.
	 */
	private static DisplayMode toDisplayMode(FrameworkDisplayMode mode) {
		final int[] depth = new int[]{mode.red, mode.green, mode.blue};
		return new Monitor.DisplayMode(new Dimensions(mode.width, mode.height), depth, mode.refresh);
	}

	public Window window(Window.Descriptor descriptor) {
		// Lookup monitor handle
		final Handle monitor;
		if(descriptor.monitor().isPresent()) {
			//monitor = (Pointer) descriptor.monitor().get().handle();
			// TODO
			monitor = null;
		}
		else {
			monitor = null;
		}

//		// Prevent GLFW creating an OpenGL context by default
//		// TODO - option in properties?
//		// glfwDefaultWindowHints
//		instance.glfwWindowHint(0x00022001, 0);

		// Apply window hints
		instance.glfwDefaultWindowHints();
		for(Window.Descriptor.Property prop : descriptor.properties()) {
			final int hint = apply(prop);
			final int flag = flag(prop);
			instance.glfwWindowHint(hint, flag);
		}

		// Create window
		final Pointer window = instance.glfwCreateWindow(descriptor.size().width(), descriptor.size().height(), descriptor.title(), monitor, null);
		return new Window(window, instance, descriptor);
	}

	/**
	 * Maps a window property to the GLFW hint.
	 */
	private static int apply(Window.Descriptor.Property prop) {
		switch(prop) {
		case RESIZABLE:			return 0x00020003;
		case DECORATED:			return 0x00020005;
		case AUTO_ICONIFY:		return 0x00020006;
		case MAXIMISED:			return 0x00020008;
		case DISABLE_OPENGL:	return 0x00022001;
		default:				throw new UnsupportedOperationException(prop.name());
		}
	}

	/**
	 * Maps a window property to the GLFW hint flag.
	 */
	private static int flag(Window.Descriptor.Property prop) {
		switch(prop) {
		case DISABLE_OPENGL:		return 0;
		default:					return 1;
		}
	}

//	public Pointer surface(Handle vulkan, Handle window) {
//		final PointerByReference ref = new PointerByReference();
//		final int result = instance.glfwCreateWindowSurface(vulkan, window, null, ref);
//		if(result != VkResult.VK_SUCCESS.value()) {
//			throw new RuntimeException("Error creating Vulkan surface: result=" + IntegerEnumeration.map(VkResult.class, result));
//		}
//		return ref.getValue();
//	}

	public void close() {
		instance.glfwTerminate();
	}
}
