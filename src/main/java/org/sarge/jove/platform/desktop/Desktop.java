package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.function.Consumer;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * The <i>desktop</i> service manages windows and monitors implemented using the GLFW native library.
 * <p>
 * Note that several GLFW operations __must__ be executed on the main thread, e.g. {@link #poll()}.
 * <p>
 * @see <a href="https://www.glfw.org/docs/latest/index.html">GLFW documentation</a>
 * @see <a href="https://github.com/badlogic/jglfw/blob/master/jglfw/jni/glfw-3.0/include/GL/glfw3.h">C header</a>
 * @see <a href="https://www.glfw.org/docs/latest/intro.html#thread_safety">Thread Constraints</a>
 * @author Sarge
 */
public class Desktop {
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
		mapper.addTypeConverter(Window.class, NativeObject.CONVERTER);

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
	 * This method __must__ be executed on the main thread.
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

	/**
	 * Destroys this desktop.
	 */
	public void close() {
		lib.glfwTerminate();
	}
}
