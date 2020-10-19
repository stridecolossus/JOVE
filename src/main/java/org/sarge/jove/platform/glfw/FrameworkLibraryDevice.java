package org.sarge.jove.platform.glfw;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * GLFW device API.
 * @author Sarge
 */
public // TODO
interface FrameworkLibraryDevice {
	/**
	 * Listener for key events.
	 */
	public // TODO
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
	 * Listener for mouse move events.
	 */
	public // TODO
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
	 * Listener for mouse button events.
	 */
	interface MouseButtonListener extends Callback {
		/**
		 * Notifies a mouse button event.
		 * @param window	window
		 * @param button	Button index 0..n
		 * @param action	Button action
		 * @param mods		Modifiers
		 * @see FrameworkLibraryDevice#operation(int)
		 */
		void button(Pointer window, int button, int action, int mods);
	}

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

	// TODO - enter/leave

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
	 * Registers a key listener.
	 * @param window		Window
	 * @param listener		Key listener
	 */
	void glfwSetKeyCallback(Pointer window, KeyListener listener);

	/**
	 * Registers a mouse movement listener.
	 * @param window		Window
	 * @param listener		Mouse movement listener
	 */
	void glfwSetCursorPosCallback(Pointer window, MousePositionListener listener);

	/**
	 * Registers a mouse button listener.
	 * @param window		Window
	 * @param listener		Mouse button listener
	 */
	void glfwSetMouseButtonCallback(Pointer window, MouseButtonListener listener);

	/**
	 * Registers a mouse scroll listener.
	 * @param window		Window
	 * @param listener		Mouse scroll listener
	 */
	void glfwSetScrollCallback(Pointer window, MouseScrollListener listener);
}
