package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

/**
 * GLFW API.
 * @author Sarge
 */
interface DesktopLibrary {
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
	 * Returns and clears the latest GLFW error.
	 * @param description Returned error description
	 * @return Error code or {@code zero} if none
	 */
	int glfwGetError(Pointer description);

	/**
	 * @return GLFW version
	 */
	String glfwGetVersionString();

	/**
	 * @return Whether Vulkan is supported on this platform
	 */
	boolean glfwVulkanSupported();

	/**
	 * Enumerates the required Vulkan extensions for this platform.
	 * @param count Number of extensions
	 * @return Vulkan extensions
	 */
	Handle glfwGetRequiredInstanceExtensions(IntegerReference count);

	/**
	 * Terminates GLFW.
	 */
	void glfwTerminate();
}
