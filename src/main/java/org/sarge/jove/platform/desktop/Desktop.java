package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.*;
import java.util.Map;
import java.util.function.Consumer;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.*;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

/**
 * The <i>desktop</i> service manages windows and input devices implemented using the GLFW native library.
 * <p>
 * Note that many GLFW operations <b>must</b> be executed on the <b>main</b> thread, these are marked by the {@link MainThread} annotation.
 * See <a href="https://www.glfw.org/docs/latest/intro.html#thread_safety">Thread Constraints</a>
 * <p>
 * @see <a href="https://www.glfw.org/docs/latest/index.html">GLFW documentation</a>
 * @see <a href="https://github.com/badlogic/jglfw/blob/master/jglfw/jni/glfw-3.0/include/GL/glfw3.h">C header</a>
 * <p>
 * @author Sarge
 */
public final class Desktop implements TransientObject {
	/**
	 * Creates the desktop service.
	 * @return New desktop service
	 * @throws RuntimeException if the GLFW native library cannot be found or cannot be initialised
	 * @throws UnsatisfiedLinkError if the native library cannot be instantiated
	 */
	@MainThread
	public static Desktop create() {
		// Determine library name
		final String name = switch(Platform.getOSType()) {
			case Platform.WINDOWS -> "C:/GLFW/lib-mingw-w64/glfw3.dll";		// <--- TODO - path!
			case Platform.LINUX -> "libglfw";
			default -> throw new RuntimeException("Unsupported platform for GLFW: " + Platform.getOSType());
		};

		// Init type mapper
		final var mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);
		mapper.addTypeConverter(Window.class, NativeObject.CONVERTER);

		// Load native library
		final DesktopLibrary lib = Native.load(name, DesktopLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, mapper));
		JoystickManager.init(lib);

		// Init GLFW
		final int result = lib.glfwInit();
		if(result != 1) throw new RuntimeException("Cannot initialise GLFW: code=" + result);

		// Create desktop service
		return new Desktop(lib, new ReferenceFactory());
	}

	/**
	 * Marker interface for methods that <b>must</b> be called from the main thread.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	@interface MainThread {
		// Marker
	}

	private final DesktopLibrary lib;
	private final ReferenceFactory factory;

	/**
	 * Constructor.
	 * @param lib 			GLFW library
	 * @param factory		Reference factory
	 */
	Desktop(DesktopLibrary lib, ReferenceFactory factory) {
		this.lib = requireNonNull(lib);
		this.factory = requireNonNull(factory);
	}

	/**
	 * @return GLFW library
	 */
	DesktopLibrary library() {
		return lib;
	}

	/**
	 * @return Reference factory
	 */
	ReferenceFactory factory() {
		return factory;
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
	@MainThread
	public void poll() {
		lib.glfwPollEvents();
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public String[] extensions() {
		final IntByReference size = factory.integer();
		final Pointer ptr = lib.glfwGetRequiredInstanceExtensions(size);
		return ptr.getStringArray(0, size.getValue());
	}

	/**
	 * Sets a handler for GLFW errors.
	 * @param handler Error handler
	 */
	@MainThread
	public void setErrorHandler(Consumer<String> handler) {
		final ErrorCallback callback = (error, description) -> {
			final String message = String.format("GLFW error: [%d] %s", error, description);
			handler.accept(message);
		};
		lib.glfwSetErrorCallback(callback);
	}

	@Override
	@MainThread
	public void destroy() {
		lib.glfwTerminate();
	}
}
