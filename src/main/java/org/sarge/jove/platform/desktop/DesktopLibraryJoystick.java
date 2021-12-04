package org.sarge.jove.platform.desktop;

import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * GLFW joystick library.
 * The joystick ID argument is in the range 0..15.
 * @author Sarge
 */
interface DesktopLibraryJoystick {
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
	Pointer glfwGetJoystickAxes(int id, IntByReference count);

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
	Pointer glfwGetJoystickButtons(int id, IntByReference count);

	/**
	 * Hat states.
	 * Note that hat diagonals are represented as a bit-mask, e.g. 3 for up-right.
	 */
	enum Hat implements IntegerEnumeration {
		CENTERED(0),
		UP(1),
		RIGHT(2),
		DOWN(4),
		LEFT(8);

		private final int value;

		private Hat(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

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
	Pointer glfwGetJoystickHats(int id, IntByReference count);

	/**
	 * Listener for joystick connection events.
	 */
	interface JoystickListener extends Callback {
		/**
		 * Notifies that a joystick has been connected or disconnected.
		 * @param id		Joystick ID
		 * @param event		Connection event
		 */
		void connect(int id, int event);
		// connected = 0x00040001
		// disconnected =  0x00040002
	}

	/**
	 * Sets the joystick connection listener.
	 * @param listener Connection listener
	 */
	void glfwSetJoystickCallback(JoystickListener listener);


	// TODO
	public static void main(String[] args) throws Exception {
		final Desktop desktop = Desktop.create();
		final var lib = desktop.library();
		final IntByReference count = new IntByReference();

//		for(int n = 0; n < 16; ++n) {
//			if(!lib.glfwJoystickPresent(n)) continue;

		System.out.println(desktop.version());
		System.out.println(lib.glfwGetJoystickName(0));

		while(true) {
			Pointer ptr = lib.glfwGetJoystickAxes(0, count);
			final float[] array = ptr.getFloatArray(0, count.getValue());
			//System.out.println(Arrays.toString(array));

//			ptr = lib.glfwGetJoystickButtons(0, count);
//			final byte[] buttons = ptr.getByteArray(0, count.getValue());
//			System.out.println(Arrays.toString(buttons));

			ptr = lib.glfwGetJoystickHats(0, count);
			final byte[] hats = ptr.getByteArray(0, count.getValue());

			System.out.println(IntegerEnumeration.mapping(Hat.class).enumerate(hats[0]));

			Thread.sleep(1000);
		}
	}
}
