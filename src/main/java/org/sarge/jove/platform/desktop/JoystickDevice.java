package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Event.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>joystick device</i> represents a joystick or HOTAS controller.
 * @author Sarge
 */
public class JoystickDevice implements Device {
	private final int id;
	private final String name;
	private final Desktop desktop;
	private final JoystickAxis[] axes;
	private final JoystickButtonSource buttons;

	// TODO - is the actual joystick itself a position event or 2 x axes???

	/**
	 * Constructor.
	 * @param id			Index
	 * @param name			Joystick name
	 * @param desktop		Desktop service
	 */
	JoystickDevice(int id, String name, Desktop desktop) {
		this.id = Check.range(id, 0, 16);
		this.name = notEmpty(name);
		this.desktop = notNull(desktop);
		this.axes = initAxes();
		this.buttons = new JoystickButtonSource(id, desktop);
	}

	/**
	 * @return Axes array initialised to the current axis positions
	 */
	private JoystickAxis[] initAxes() {
		final float[] array = getAxisArray();
		final JoystickAxis[] axes = new JoystickAxis[array.length];
		Arrays.setAll(axes, n -> new JoystickAxis(n, array[n]));
		return axes;
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
	public List<Axis> axes() {
		return Arrays.asList(axes);
	}

	/**
	 * @return Event source for the buttons and hats of this device
	 */
	public JoystickButtonSource buttons() {
		return buttons;
	}

	@Override
	public Set<Source<?>> sources() {
		final Set<Source<?>> sources = new HashSet<>();
		sources.addAll(axes());
		sources.add(buttons);
		return sources;
	}

	/**
	 * Polls joystick events.
	 */
	void poll() {
		pollAxes();
		buttons.poll();
	}

	/**
	 * Polls axis events.
	 */
	private void pollAxes() {
		// TODO - only need to do 'count' once
		final float[] array = getAxisArray();
		for(int n = 0; n < array.length; ++n) {
			axes[n].update(array[n]);
		}
	}

	/**
	 * Queries the axis values for this joystick.
	 */
	private float[] getAxisArray() {
		final IntByReference count = desktop.factory().integer();
		final Pointer ptr = desktop.library().glfwGetJoystickAxes(id, count);
		return ptr.getFloatArray(0, count.getValue());
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
