package org.sarge.jove.platform.desktop;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * GLFW API.
 * @author Sarge
 */
interface DesktopLibrary extends Library, DesktopLibraryWindow, DesktopLibraryMonitor, DesktopLibraryDevice, DesktopLibraryJoystick {
	/**
	 * Sets an initialisation hint.
	 * @param hint		Hint
	 * @param value		Value
	 */
	void glfwInitHint(int hint, int value);

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
	 * @param count Number of results
	 * @return Vulkan extensions (pointer to array of strings)
	 */
	Pointer glfwGetRequiredInstanceExtensions(IntByReference count);
}
