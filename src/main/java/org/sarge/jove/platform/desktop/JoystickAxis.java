package org.sarge.jove.platform.desktop;

import static org.sarge.lib.Validation.requireZeroOrMore;

import java.util.function.Consumer;

import org.sarge.jove.control.AxisControl;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>joystick axis</i> is a controller for a ranged value such as a HOTAS throttle.
 * @author Sarge
 */
class JoystickAxis extends AxisControl {
	private final int index;
	private Consumer<AxisControl> handler;

	/**
	 * Constructor.
	 * @param index Axis index
	 * @param value Initial position
	 */
	JoystickAxis(int index, float value) {
		this.index = requireZeroOrMore(index);
		update(value);
	}

	@Override
	public String name() {
		return String.format("Joystick-Axis-%d", index);
	}

	@Override
	public Object bind(Consumer<AxisControl> handler) {
		this.handler = handler;
		return null;
	}

	@Override
	public void update(float value) {
		// Ignore if not modified
		if(MathsUtility.isApproxEqual(this.value(), value)) {
			return;
		}

		// Record modified value
		super.update(value);

		// Generate event
		handler.accept(this);
	}

	@Override
	public int hashCode() {
		return index;
	}
}
