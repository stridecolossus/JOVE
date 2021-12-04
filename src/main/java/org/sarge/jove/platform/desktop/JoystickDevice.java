package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;
import org.sarge.lib.util.Check;

public class JoystickDevice implements Device {

	public interface ConnectionListener {
		void connect(JoystickDevice joystick, boolean connected);
	}

	/**
	 *
	 */
	public static class Manager {
		private final DesktopLibrary lib;

		/**
		 * Constructor.
		 * @param lib GLFW library
		 */
		Manager(DesktopLibrary lib) {
			this.lib = notNull(lib);
		}

		/**
		 * @return List of joystick devices
		 */
		public List<JoystickDevice> devices() {
			return IntStream
					.range(0, 16)
					.filter(lib::glfwJoystickPresent)
					.mapToObj(this::create)
					.collect(toList());
		}

		private JoystickDevice create(int id) {

			final String name = lib.glfwGetJoystickName(id);



			return null; //new JoystickDevice(id, name, null);
		}

		/**
		 * Sets the listener for joystick connection events.
		 * @param listener Connection listener
		 */
		public void listener(ConnectionListener listener) {
			//lib.glfwSetJoystickCallback(callback);
		}
	}

	private final int id;
	private final String name;
	private final Set<Source> sources;
	private final float[] axes;
	private final Action[] buttons;

	/**
	 * Constructor.
	 * @param id			Index
	 * @param name			Joystick name
	 * @param sources		Event sources
	 */
	private JoystickDevice(int id, String name, float[] axes, Action[] buttons) {
		this.id = Check.range(id, 0, 16);
		this.name = notEmpty(name);
		this.axes = notNull(axes);
		this.buttons = notNull(buttons);
		this.sources = null; //build(axes, buttons);
	}

	private Source axis(float value) {
		return null;
	}

	/**
	 * @return Name of this joystick
	 */
	public String name() {
		return name;
	}

	public float[] axes() {
		return null;
	}

	public Action[] buttons() {
		return null;
	}

	@Override
	public Set<Source> sources() {
		return sources;
	}
}
