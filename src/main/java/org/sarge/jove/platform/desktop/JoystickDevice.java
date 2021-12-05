package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryJoystick.JoystickListener;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>joystick device</i> represents a joystick or HOTAS controller.
 * @author Sarge
 */
public class JoystickDevice implements Device {
	/**
	 * A <i>joystick axis</i> is a variable controller such as a HOTAS throttle.
	 */
	public static class JoystickAxis extends AbstractSource<AxisEvent> implements Axis {
		private final int index;
		private float value;

		/**
		 * Constructor.
		 * @param index Axis index
		 * @param value Initial position
		 */
		JoystickAxis(int index, float value) {
			this.index = zeroOrMore(index);
			this.value = value;
		}

		/**
		 * @return Current axis position
		 */
		public float value() {
			return value;
		}

		/**
		 * Updates this axis and generates events accordingly.
		 * @param value Axis position
		 */
		void update(float value) {
			// Ignore if not modified
			if(MathsUtil.isEqual(this.value, value)) {
				return;
			}

			// Record modified value
			this.value = value;

			// Generate event
			if(handler == null) {
				return;
			}
			final AxisEvent event = new AxisEvent(this, value);
			handler.accept(event);
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(value).build();
		}
	}

	/**
	 * Event source for joystick buttons.
	 */
	private class ButtonSource extends AbstractSource<Button> {
		private void poll() {
			// Ignore if no action handler
			if(handler == null) {
				return;
			}

			// Retrieve button values
			final byte[] values = buttons(id, lib);

			// Generate button events
			for(int n = 0; n < values.length; ++n) {
				// TODO - hats
				if(values[n] == 1) {
					handler.accept(buttons[n]);
				}
			}
		}
	}

	private final int id;
	private final String name;
	private final JoystickAxis[] axes;
	private final Button[] buttons;
	private final ButtonSource src = new ButtonSource();
	private final DesktopLibraryJoystick lib;

	/**
	 * Constructor.
	 * @param id			Index
	 * @param name			Joystick name
	 * @param axes			Axes
	 * @param buttons		Buttons
	 * @param lib			GLFW library
	 */
	JoystickDevice(int id, String name, JoystickAxis[] axes, Button[] buttons, DesktopLibraryJoystick lib) {
		this.id = Check.range(id, 0, 16);
		this.name = notEmpty(name);
		this.axes = notNull(axes);
		this.buttons = notNull(buttons);
		this.lib = notNull(lib);
	}

	/**
	 * @return Name of this joystick
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Joystick axes
	 */
	public List<JoystickAxis> axes() {
		return Arrays.asList(axes);
	}

	/**
	 * @return Joystick buttons and hats
	 */
	public List<Button> buttons() {
		return Arrays.asList(buttons);
	}

	/**
	 * @return Buttons event source
	 */
	public Source<Button> buttonSource() {
		return src;
	}

	@Override
	public Set<Source<?>> sources() {
		final Set<Source<?>> sources = new HashSet<>();
		sources.addAll(axes());
		sources.add(src);
		return sources;
	}

	/**
	 * Polls joystick events.
	 */
	void poll() {
		// Retrieve axis values
		final float[] array = axes(id, lib);

		// Update modified axes
		for(int n = 0; n < array.length; ++n) {
			axes[n].update(array[n]);
		}

		// Poll buttons
		src.poll();
	}

	/**
	 * Queries the axis values for the given joystick.
	 * @param id		Joystick ID
	 * @param lib		GLFW library
	 * @return Axis values
	 */
	private static float[] axes(int id, DesktopLibraryJoystick lib) {
		final IntByReference count = new IntByReference();
		final Pointer ptr = lib.glfwGetJoystickAxes(id, count);
		return ptr.getFloatArray(0, count.getValue());
	}

	/**
	 * Queries the button values for the given joystick.
	 * @param id		Joystick ID
	 * @param lib		GLFW library
	 * @return Button values
	 */
	private static byte[] buttons(int id, DesktopLibraryJoystick lib) {
		final IntByReference count = new IntByReference();
		final Pointer ptr = lib.glfwGetJoystickButtons(id, count);
		return ptr.getByteArray(0, count.getValue());
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof JoystickDevice that) &&
				(this.id == that.id);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(id)
				.append(name)
				.build();
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

	/**
	 * The <i>joystick manager</i> manages the joystick devices attached to the system.
	 */
	public static class Manager {
		private final DesktopLibraryJoystick lib;
		private final Collection<JoystickDevice> devices = new ArrayList<>();

		/**
		 * Constructor.
		 * @param desktop Desktop service
		 */
		public Manager(Desktop desktop) {
			this.lib = desktop.library();
			init();
		}

		/**
		 * Initialises devices.
		 */
		private void init() {
			IntStream
					.range(0, 16)
					.filter(lib::glfwJoystickPresent)
					.mapToObj(this::create)
					.forEach(devices::add);
		}

		/**
		 * Removes a disconnected device.
		 * @param id Device ID
		 * @return Removed device
		 */
		private JoystickDevice remove(int id) {
			final JoystickDevice prev = devices
					.stream()
					.filter(dev -> dev.id == id)
					.findAny()
					.orElseThrow();

			devices.remove(prev);

			return prev;
		}

		/**
		 * Creates a joystick device.
		 * @param id Joystick ID
		 * @return New joystick
		 */
		private JoystickDevice create(int id) {
			// Retrieve joystick name
			final String name = lib.glfwGetJoystickName(id);

			// Init joystick axis values
			final float[] array = axes(id, lib);
			final JoystickAxis[] axes = new JoystickAxis[array.length];
			Arrays.setAll(axes, n -> new JoystickAxis(n, array[n]));

			// Init buttons
			final int count = buttons(id, lib).length;
			final Button[] buttons = IntStream
					.range(0, count)
					.mapToObj(n -> Button.name("Button", n))
					.map(Button::new)
					.toArray(Button[]::new);

			// Create joystick
			return new JoystickDevice(id, name, axes, buttons, lib);
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
}
