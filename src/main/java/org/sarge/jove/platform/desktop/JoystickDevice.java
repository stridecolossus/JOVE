package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>joystick device</i> represents a joystick or HOTAS controller.
 * @author Sarge
 */
public class JoystickDevice implements Device {
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
	 * @return Joystick ID
	 */
	int id() {
		return id;
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
	static float[] axes(int id, DesktopLibraryJoystick lib) {
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
	static byte[] buttons(int id, DesktopLibraryJoystick lib) {
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
}
