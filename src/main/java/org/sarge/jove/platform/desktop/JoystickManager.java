package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.IntStream;

import org.sarge.jove.platform.desktop.DesktopLibraryJoystick.JoystickListener;

/**
 * The <i>joystick manager</i> manages the joystick devices attached to the system.
 * @author Sarge
 */
public class JoystickManager {
	/**
	 * Disables joystick hats being present in the buttons array
	 */
	static void init(DesktopLibrary lib) {
    	// Disable joystick hats being present in the buttons array
    	lib.glfwInitHint(0x00050001, 0);
	}

	/**
	 * A <i>connection listener</i> notifies joystick connection and disconnection events.
	 */
	public interface ConnectionListener {
		/**
		 * Notifies a joystick connection event.
		 * @param joystick			Joystick
		 * @param connected			Whether connected or disconnected
		 */
		void connect(JoystickDevice joystick, boolean connected);
	}

	private final Desktop desktop;
	private final Collection<JoystickDevice> devices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param desktop Desktop service
	 */
	public JoystickManager(Desktop desktop) {
		this.desktop = notNull(desktop);
		init();
	}

	/**
	 * Initialises devices.
	 */
	private void init() {
		final DesktopLibraryJoystick lib = desktop.library();
		IntStream
				.range(0, 16)
				.filter(lib::glfwJoystickPresent)
				.mapToObj(this::create)
				.forEach(devices::add);
	}

	/**
	 * Creates a joystick device.
	 * @param id Joystick ID
	 * @return New joystick
	 */
	private JoystickDevice create(int id) {
		final String name = desktop.library().glfwGetJoystickName(id);
		return new JoystickDevice(id, name, desktop);
	}

	/**
	 * Removes a disconnected device.
	 * @param id Device ID
	 * @return Removed device
	 */
	private JoystickDevice remove(int id) {
		final JoystickDevice prev = devices
				.stream()
				.filter(dev -> dev.id() == id)
				.findAny()
				.orElseThrow();

		devices.remove(prev);

		return prev;
	}

	/**
	 * Enumerates the joystick devices attached to this system.
	 * @return Joystick devices
	 */
	public List<JoystickDevice> devices() {
		return List.copyOf(devices);
	}

	/**
	 * Polls joystick events.
	 */
	public void poll() {
		for(JoystickDevice dev : devices) {
			dev.poll();
		}
	}

	/**
	 * Sets the listener for joystick connection events.
	 * @param listener Connection listener or {@code null} to disable
	 */
	public void listener(ConnectionListener listener) {
		// Disable listener
		final DesktopLibraryJoystick lib = desktop.library();
		if(listener == null) {
			lib.glfwSetJoystickCallback(null);
			return;
		}

		// Register adapter that maintains device array
		final JoystickListener adapter = (id, event) -> {
			// Determine event
			final boolean connected = event == 0x00040001;

			// Register device
			if(connected) {
				final JoystickDevice dev = create(id);
				devices.add(dev);
				listener.connect(dev, true);
			}
			else {
				final JoystickDevice prev = remove(id);
				listener.connect(prev, false);
			}

			// Notify event
			listener.connect(null, connected);
		};
		lib.glfwSetJoystickCallback(adapter);
	}
}
