package org.sarge.jove.platform.desktop;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * GLFW device API.
 * @author Sarge
 */
interface DesktopLibraryDevice {
	/**
	 * Processes pending events.
	 */
	void glfwPollEvents();

	/**
	 * Looks up the localised name of the given key.
	 * @param key			Key
	 * @param scancode		Scan code
	 * @return Key name
	 */
	String glfwGetKeyName(int key, int scancode);

	/**
	 * Listener for key events.
	 */
	interface KeyListener extends Callback {
		/**
		 * Notifies a key event.
		 * @param window			Window
		 * @param key				Key index
		 * @param scancode			Key scan code
		 * @param action			Key action
		 * @param mods				Modifiers
		 */
		void key(Pointer window, int key, int scancode, int action, int mods);
	}

	/**
	 * Registers a key listener.
	 * @param window		Window
	 * @param listener		Key listener
	 */
	void glfwSetKeyCallback(Window window, KeyListener listener);

	/**
	 * Listener for mouse move events.
	 */
	interface MousePositionListener extends Callback {
		/**
		 * Notifies a mouse movement event.
		 * @param window	Window
		 * @param x			X coordinate
		 * @param y			Y coordinate
		 */
		void move(Pointer window, double x, double y);
	}

	/**
	 * Registers a mouse movement listener.
	 * @param window		Window
	 * @param listener		Mouse movement listener
	 */
	void glfwSetCursorPosCallback(Window window, MousePositionListener listener);

	/**
	 * Listener for mouse button events.
	 */
	interface MouseButtonListener extends Callback {
		/**
		 * Notifies a mouse button event.
		 * @param window	window
		 * @param button	Button index 0..n
		 * @param action	Button action
		 * @param mods		Modifiers
		 */
		void button(Pointer window, int button, int action, int mods);
	}

	/**
	 * Registers a mouse button listener.
	 * @param window		Window
	 * @param listener		Mouse button listener
	 */
	void glfwSetMouseButtonCallback(Window window, MouseButtonListener listener);

	/**
	 * Listener for mouse scroll events.
	 */
	interface MouseScrollListener extends Callback {
		/**
		 * Notifies a mouse scroll event.
		 * @param window	Window
		 * @param x			X offset
		 * @param y			Y offset
		 */
		void scroll(Pointer window, double x, double y);
	}

	/**
	 * Registers a mouse scroll listener.
	 * @param window		Window
	 * @param listener		Mouse scroll listener
	 */
	void glfwSetScrollCallback(Window window, MouseScrollListener listener);
}
