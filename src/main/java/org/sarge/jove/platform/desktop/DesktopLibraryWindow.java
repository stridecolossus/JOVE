package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * GLFW window API.
 * @author Sarge
 */
interface DesktopLibraryWindow {
	/**
	 * Creates a window.
	 * @param w				Width
	 * @param h				Height
	 * @param title			Window title
	 * @param monitor		Monitor for a full-screen window
	 * @param shared		Optional shared window
	 * @return Window handle
	 */
	Pointer glfwCreateWindow(int w, int h, String title, Handle monitor, Handle shared);

	/**
	 * Destroys a window.
	 * @param window Window handle
	 */
	void glfwDestroyWindow(Handle window);

	/**
	 * Resets all window hints to their default values.
	 */
	void glfwDefaultWindowHints();

	/**
	 * Sets a creation window hint.
	 * @param hint		Hint
	 * @param value		Value
	 */
	void glfwWindowHint(int hint, int value);

	/**
	 * Creates a Vulkan surface for the given window.
	 * @param instance			Vulkan instance
	 * @param window			Window handle
	 * @param allocator			Allocator
	 * @param surface			Returned surface handle
	 * @return Result
	 */
	int glfwCreateWindowSurface(Handle instance, Handle window, Handle allocator, PointerByReference surface);

	// glfwWindowShouldClose(window)
	// glfwSetWindowCloseCallback(window, window_close_callback);
	// glfwSetWindowSize(window, 640, 480);
	// glfwSetWindowSizeCallback(window, window_size_callback);
	// glfwSetWindowTitle(window, u8"This is always a UTF-8 string");
	// GLFWmonitor* monitor = glfwGetWindowMonitor(window);
	// swap buffers?
}
