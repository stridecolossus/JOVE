package org.sarge.jove.platform.desktop;

import org.sarge.jove.foreign.*;

/**
 * GLFW API.
 * @author Sarge
 */
interface DesktopLibrary extends DesktopLibraryWindow { // , DesktopLibraryDevice { // TODO , DesktopLibraryMonitor, DesktopLibraryJoystick {
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

//	/**
//	 * Error callback.
//	 */
//	interface ErrorCallback extends Callback {
//		/**
//		 * Notifies a GLFW error.
//		 * @param error				Error code
//		 * @param description		Description
//		 */
//		void error(int error, String description);
//	}
//
//	/**
//	 * Registers an error handler.
//	 * @param callback Error handler
//	 */
//	void glfwSetErrorCallback(ErrorCallback callback);
// TODO

	int glfwGetError(String description); // TODO - ? NativeReference<String>

	/**
	 * @return GLFW version
	 */
	String glfwGetVersionString();

	/**
	 * @return Whether vulkan is supported on this platform
	 */
	boolean glfwVulkanSupported();

	/**
	 * Enumerates the required Vulkan extensions for this platform.
	 * @param count Number of extensions
	 * @return Vulkan extensions
	 */
	ReturnedArray<String> glfwGetRequiredInstanceExtensions(NativeReference<Integer> count);
}
