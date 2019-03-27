package org.sarge.jove.platform.glfw;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * GLFW API.
 * @author Sarge
 */
interface FrameworkLibrary extends Library, FrameworkLibraryWindow, FrameworkLibraryDevice, FrameworkLibraryMonitor {
	/**
	 * Creates a GLFW instance.
	 * @throws RuntimeException if the instance cannot be created
	 */
	static FrameworkLibrary create() {
//		return Native.load("C:/GLFW/lib-mingw-w64/glfw3.dll", FrameworkLibrary.class);
		return Native.load(library(), FrameworkLibrary.class);
	}

	private static String library() {
		switch(Platform.getOSType()) {
		case Platform.WINDOWS:		return "C:/GLFW/lib-mingw-w64/glfw3.dll";		// <--- TODO
		case Platform.LINUX:		return "libglfw";
		default:					throw new UnsupportedOperationException("Unsupported platform for GLFW: " + Platform.getOSType());
		}
	}

	/**
	 * Initialises GLFW.
	 * @return Success code
	 */
	int glfwInit();

	/**
	 * Terminates GLFW.
	 */
	void glfwTerminate();

	/**
	 * Error callback.
	 */
	interface ErrorCallback extends Callback {
		/**
		 * Notifies a GLFW error.
		 * @param error				Error code
		 * @param description		Description
		 */
		void error(int error, String description);
	}

	/**
	 * Registers an error handler.
	 * @param callback Error handler
	 */
	void glfwSetErrorCallback(ErrorCallback callback);

	/**
	 * @return GLFW version
	 */
	String glfwGetVersionString();

	/**
	 * @return Whether vulkan is supported on this platform
	 */
	boolean glfwVulkanSupported();

	/**
	 * Enumerates the required vulkan extensions for this platform.
	 * @param count Size of results
	 * @return
	 */
	PointerByReference glfwGetRequiredInstanceExtensions(IntByReference size);

	/**
	 * Creates a Vulkan surface for the given window.
	 * @param instance			Vulkan instance
	 * @param window			Window handle
	 * @param allocator			Allocator
	 * @param surface			Returned surface handle
	 * @return Result
	 */
	int glfwCreateWindowSurface(Pointer instance, Pointer window, Pointer allocator, PointerByReference surface);
}
