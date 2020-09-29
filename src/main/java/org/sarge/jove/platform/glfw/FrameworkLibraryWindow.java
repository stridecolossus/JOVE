package org.sarge.jove.platform.glfw;

import org.sarge.jove.common.Handle;

import com.sun.jna.Pointer;

/**
 * GLFW window API.
 * @author Sarge
 */
interface FrameworkLibraryWindow {
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

	// glfwWindowShouldClose(window)
	// glfwSetWindowCloseCallback(window, window_close_callback);
	// glfwSetWindowSize(window, 640, 480);
	// glfwSetWindowSizeCallback(window, window_size_callback);
	// glfwSetWindowTitle(window, u8"This is always a UTF-8 string");
	// GLFWmonitor* monitor = glfwGetWindowMonitor(window);
	// swap buffers?
}
