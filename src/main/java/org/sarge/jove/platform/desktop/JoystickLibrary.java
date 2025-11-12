package org.sarge.jove.platform.desktop;

import java.awt.Button;

import org.sarge.jove.foreign.IntegerReference;

/**
 * GLFW joystick library.
 * The joystick ID argument is in the range 0..15.
 * @author Sarge
 */
interface JoystickLibrary {
	/**
	 * Tests whether a joystick is present.
	 * @param id Joystick ID
	 * @return Whether the given joystick is present
	 */
	boolean glfwJoystickPresent(int id);

	/**
	 * Retrieves the name of a joystick.
	 * @param id Joystick ID
	 * @return Name
	 */
	String glfwGetJoystickName(int id);

	/**
	 * Retrieves the axis states of a joystick.
	 * <p>
	 * The axis states is an array of floating-point values in the range -1 to +1.
	 * <p>
	 * @param id 		Joystick ID
	 * @param count		Number of axes
	 * @return Axis states
	 */
	float[] glfwGetJoystickAxes(int id, IntegerReference count);
	// TODO - cannot return array

	/**
	 * Retrieves the button states of a joystick.
	 * <p>
	 * The returned data is an array of bytes mapping to the {@link Button.Action} enumeration.
	 * <p>
	 * Note that this also includes joystick hats (see {@link #glfwGetJoystickHats(int, IntByReference)}).
	 * <p>
	 * @param id 		Joystick ID
	 * @param count		Number of buttons
	 * @return Button states
	 */
	float[] glfwGetJoystickButtons(int id, IntegerReference count);
	// TODO - cannot return array

	/**
	 * Retrieves the hat states of a joystick.
	 * <p>
	 * The returned data is an array of byte bit-masks mapped to the {@link Hat} enumeration.
	 * <p>
	 * @param id 		Joystick ID
	 * @param count		Number of hats
	 * @return Hat states
	 * @see Hat
	 */
	float[] glfwGetJoystickHats(int id, IntegerReference count);
	// TODO - cannot return array

	/**
	 * Listener for joystick connection events.
	 */
	interface JoystickListener {
		/**
		 * Notifies that a joystick has been connected or disconnected.
		 * @param id		Joystick ID
		 * @param event		Connection event
		 */
		void connect(int id, int event);
	}

	/**
	 * Sets the joystick connection listener.
	 * @param listener Connection listener
	 */
	void glfwSetJoystickCallback(JoystickListener listener);
}
