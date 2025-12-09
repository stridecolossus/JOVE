package org.sarge.jove.platform.desktop;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.foreign.Callback;

/**
 * GLFW device API.
 * @author Sarge
 */
public
interface DeviceLibrary {
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
	@FunctionalInterface
	interface KeyListener extends Callback {
		/**
		 * Notifies a key event.
		 * @param window			Window
		 * @param key				Key index
		 * @param scancode			Key scan code
		 * @param action			Key action
		 * @param mods				Modifiers
		 */
		void key(MemorySegment window, int key, int scancode, int action, int mods);
	}

	/**
	 * Registers a key listener.
	 * @param window		Window
	 * @param listener		Key listener
	 */
	void glfwSetKeyCallback(Window window, KeyListener listener);

	/**
	 * Listener for mouse pointer and scroll wheel events.
	 */
	@FunctionalInterface
	interface MouseListener extends Callback {
		/**
		 * Notifies a mouse event.
		 * @param window	Window
		 * @param x			X coordinate
		 * @param y			Y coordinate
		 */
		void event(MemorySegment window, double x, double y);
	}

	/**
	 * Registers a mouse movement listener.
	 * @param window		Window
	 * @param listener		Mouse movement listener
	 */
	void glfwSetCursorPosCallback(Window window, MouseListener listener);

	/**
	 * Registers a mouse scroll listener.
	 * @param window		Window
	 * @param listener		Mouse scroll listener
	 */
	void glfwSetScrollCallback(Window window, MouseListener listener);

	/**
	 * Listener for mouse button events.
	 */
	@FunctionalInterface
	interface MouseButtonListener extends Callback {
		/**
		 * Notifies a mouse button event.
		 * @param window	window
		 * @param button	Button index 0..n
		 * @param action	Button action
		 * @param mods		Modifiers
		 */
		void button(MemorySegment window, int button, int action, int mods);
	}

	/**
	 * Registers a mouse button listener.
	 * @param window		Window
	 * @param listener		Mouse button listener
	 */
	void glfwSetMouseButtonCallback(Window window, MouseButtonListener listener);
}
