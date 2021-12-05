package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopButton.Action;
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
			final byte[] values = getButtonArray();

			// Generate events for modified buttons
			for(int n = 0; n < values.length; ++n) {
				// Skip if not changed
				final Action action = Action.map(values[n]);
				if(buttons[n].action() == action) {
					continue;
				}

				// Update button state
				buttons[n] = buttons[n].resolve(action);

				// Generate event
				handler.accept(buttons[n]);
			}
			// TODO - lots of array[n], move to local class?
		}
	}

	private final int id;
	private final String name;
	private final JoystickAxis[] axes;
	private final DesktopButton[] buttons;
	private final ButtonSource src = new ButtonSource();
	private final DesktopLibraryJoystick lib;

	/**
	 * Constructor.
	 * @param id			Index
	 * @param name			Joystick name
	 * @param lib			GLFW library
	 */
	JoystickDevice(int id, String name, DesktopLibraryJoystick lib) {
		this.id = Check.range(id, 0, 16);
		this.name = notEmpty(name);
		this.lib = notNull(lib);
		this.axes = initAxes();
		this.buttons = initButtons();
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
	 * @return Buttons array
	 */
	private DesktopButton[] initButtons() {
		final byte[] array = getButtonArray();
		final DesktopButton[] buttons = new DesktopButton[array.length];
		for(int n = 0; n < buttons.length; ++n) {
			final String name = Button.name("Button", n);
			buttons[n] = new DesktopButton(name);
		}
		return buttons;
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
		pollAxes();
		src.poll();
	}

	private void pollAxes() {
		final float[] array = getAxisArray();
		for(int n = 0; n < array.length; ++n) {
			axes[n].update(array[n]);
		}
	}

	/**
	 * Queries the axis values for this joystick.
	 */
	private float[] getAxisArray() {
		final IntByReference count = new IntByReference();
		final Pointer ptr = lib.glfwGetJoystickAxes(id, count);
		return ptr.getFloatArray(0, count.getValue());
	}

	/**
	 * Queries the button values for this joystick.
	 */
	private byte[] getButtonArray() {
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
