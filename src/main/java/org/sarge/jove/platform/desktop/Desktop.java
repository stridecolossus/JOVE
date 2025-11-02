package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.*;
import java.util.List;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.foreign.*;

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
	 * @return Desktop service
	 * @throws RuntimeException if GLFW cannot be loaded or initialised
	 */
	@MainThread
	public static Desktop create() {
//		// Determine library name
//		final String name = switch(Platform.getOSType()) {
//			case Platform.WINDOWS -> "C:/GLFW/lib-mingw-w64/glfw3.dll";		// <--- TODO - path!
//			case Platform.LINUX -> "libglfw";
//			default -> throw new RuntimeException("Unsupported platform for GLFW: " + Platform.getOSType());
//		};

		// TODO
		final var registry = DefaultRegistry.create();
		//registry.add(DeviceListener.class, null);

		// Load native library
		final var builder = new NativeLibrary.Builder("C:/GLFW/lib-mingw-w64/glfw3.dll", registry); // TODO - name
		final DesktopLibrary lib = builder.build(List.of(DesktopLibrary.class)).get();
		// TODO - JoystickManager.init(lib);

		// Init GLFW
		final int result = lib.glfwInit();
		if(result != 1) {
			throw new RuntimeException("Cannot initialise GLFW: code=" + result);
		}

		// Create desktop service
		return new Desktop(lib);
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

	/**
	 * Constructor.
	 * @param lib GLFW library
	 */
	Desktop(DesktopLibrary lib) {
		this.lib = requireNonNull(lib);
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
	@MainThread
	public void poll() {
//		lib.glfwPollEvents();
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public String[] extensions() {
//		final NativeReference<Integer> count = factory.integer();
//		final ReturnedArray<String> array = lib.glfwGetRequiredInstanceExtensions(count);
//		return array.get(count.get(), String.class);
		return new String[]{
				"VK_KHR_surface",
				"VK_KHR_win32_surface",
		};
	}

//	/**
//	 * Sets the handler for GLFW errors.
//	 * @param handler Error handler
//	 */
//	@MainThread
//	public void setErrorHandler(Consumer<String> handler) {
//		final ErrorCallback callback = (error, description) -> {
//			final String message = String.format("GLFW error: [%d] %s", error, description);
//			handler.accept(message);
//		};
//		lib.glfwSetErrorCallback(callback);
//	}

	@Override
	@MainThread
	public void destroy() {
		lib.glfwTerminate();
	}
}
