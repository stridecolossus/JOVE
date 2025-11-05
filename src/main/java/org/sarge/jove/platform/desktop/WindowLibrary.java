package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

/**
 * GLFW window API.
 * @author Sarge
 */
public interface WindowLibrary {
	/**
	 * Resets all window hints to the default values.
	 */
	void glfwDefaultWindowHints();

	/**
	 * Sets a creation window hint to be applied to the next window.
	 * @param hint		Hint
	 * @param value		Value
	 */
	void glfwWindowHint(int hint, int value);

	/**
	 * Creates a new window.
	 * @param w				Width
	 * @param h				Height
	 * @param title			Window title
	 * @param monitor		Monitor for a full-screen window
	 * @param shared		Optional shared window
	 * @return Window
	 */
	Handle glfwCreateWindow(int w, int h, String title, Handle /*Monitor*/ monitor, Window shared);
	// TODO - returns WINDOW???

	/**
	 * Creates a Vulkan surface for the given window.
	 * @param instance			Vulkan instance
	 * @param window			Window handle
	 * @param allocator			Allocator
	 * @param surface			Returned surface handle
	 * @return Result
	 */
	int glfwCreateWindowSurface(Handle instance, Window window, Handle allocator, Pointer surface);

	/**
	 * @param window Window
	 * @return Whether the given window can be closed by the user
	 */
	boolean glfwWindowShouldClose(Window window);

//	/**
//	 * Sets whether the given window can be closed by the user.
//	 * @param window		Window
//	 * @param close			Whether the window can be closed
//	 * @return Result
//	 */
//	int glfwSetWindowShouldClose(Window window, boolean close);

	/**
	 * Sets the title of the given window.
	 * @param window		Window
	 * @param title			New title
	 */
	void glfwSetWindowTitle(Window window, String title);

	/**
	 * Retrieves the dimensions of the given window.
	 * @param window	Window
	 * @param w			Width
	 * @param h			Height
	 */
	void glfwGetWindowSize(Window window, IntegerReference w, IntegerReference h);

	/**
	 * Sets the dimensions of the given window.
	 * @param window	Window to resize
	 * @param w			Width
	 * @param h			Height
	 */
	void glfwSetWindowSize(Window window, int w, int h);

//	/**
//	 * Retrieves the monitor for a full sized window.
//	 * @param window Window
//	 * @return Monitor or {@code null} if not full screen
//	 */
//	Monitor glfwGetWindowMonitor(Window window);

	/**
	 * Listener for window events represented by a boolean state, e.g. window focus.
	 */
	@FunctionalInterface
	interface WindowStateListener {
		/**
		 * Notifies that a window state change.
		 * @param window		Window
		 * @param state			State
		 */
		void state(Handle window, int state);
	}

//	void glfwSetWindowCloseCallback(Window window, WindowStateListener listener);
//
//	/**
//	 * Sets the focus listener of a window.
//	 * @param window		Window
//	 * @param listener		Focus listener
//	 */
//	void glfwSetWindowFocusCallback(Window window, WindowStateListener listener);
//
//	/**
//	 * Registers a cursor enter/leave listener.
//	 * @param window		Window
//	 * @param listener		Cursor listener
//	 */
//	void glfwSetCursorEnterCallback(Window window, WindowStateListener listener);
//
//	/**
//	 * Sets the iconify listener of a window.
//	 * @param window		Window
//	 * @param listener		Iconify listener
//	 */
//	void glfwSetWindowIconifyCallback(Window window, WindowStateListener listener);

	/**
	 * Listener for window resize events.
	 */
	interface WindowResizeListener {
		/**
		 * Notifies a window resize event.
		 * @param window		Window
		 * @param width			Width
		 * @param height		Height
		 */
		void resize(Handle window, int width, int height);
	}

//	/**
//	 * Sets the resize listener of a window.
//	 * @param window		Window
//	 * @param listener		Resize listener
//	 */
//	void glfwSetWindowSizeCallback(Window window, WindowResizeListener listener);

	/**
	 * Destroys the given window.
	 * @param window Window to destroy
	 */
	void glfwDestroyWindow(Window window);
}
