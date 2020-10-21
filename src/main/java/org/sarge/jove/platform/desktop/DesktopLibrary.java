package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.NativeObject.Handle;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * GLFW API.
 * @author Sarge
 */
interface DesktopLibrary extends Library, DesktopLibraryWindow, DesktopLibraryDevice, DesktopLibraryMonitor {
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
	int glfwCreateWindowSurface(Handle instance, Handle window, Handle allocator, PointerByReference surface);
}
