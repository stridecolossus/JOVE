package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;

import com.sun.jna.Callback;
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
	Pointer glfwCreateWindow(int w, int h, String title, Monitor monitor, Window shared);

	/**
	 * Destroys a window.
	 * @param window Window handle
	 */
	void glfwDestroyWindow(Window window);

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
	int glfwCreateWindowSurface(Handle instance, Window window, Pointer allocator, PointerByReference surface);

	// TODO - other methods
	// glfwWindowShouldClose(window)
	// glfwSetWindowCloseCallback(window, window_close_callback);
	// glfwSetWindowSize(window, 640, 480);
	// glfwSetWindowTitle(window, u8"This is always a UTF-8 string");
	// GLFWmonitor* monitor = glfwGetWindowMonitor(window);

	/**
	 * Listener for window events represented by a boolean state, e.g. window focus.
	 */
	@FunctionalInterface
	interface WindowStateListener extends Callback {
		/**
		 * Notifies that a window state change.
		 * @param window		Window
		 * @param state			State
		 */
		void state(Pointer window, boolean state);
	}

	/**
	 * Sets the focus listener of a window.
	 * @param window		Window
	 * @param listener		Focus listener
	 */
	void glfwSetWindowFocusCallback(Window window, WindowStateListener listener);

	/**
	 * Registers a cursor enter/leave listener.
	 * @param window		Window
	 * @param listener		Cursor listener
	 */
	void glfwSetCursorEnterCallback(Window window, WindowStateListener listener);

	/**
	 * Sets the iconify listener of a window.
	 * @param window		Window
	 * @param listener		Iconify listener
	 */
	void glfwSetWindowIconifyCallback(Window window, WindowStateListener listener);

	/**
	 * Listener for window resize events.
	 */
	interface WindowResizeListener extends Callback {
		/**
		 * Notifies a window resize event.
		 * @param window		Window
		 * @param width			Width
		 * @param height		Height
		 */
		void resize(Pointer window, int width, int height);
	}

	/**
	 * Sets the resize listener of a window.
	 * @param window		Window
	 * @param listener		Resize listener
	 */
	void glfwSetWindowSizeCallback(Window window, WindowResizeListener listener);
}
