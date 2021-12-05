package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.zeroOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.AbstractSource;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>joystick axis</i> is a controller for a ranged value such as a HOTAS throttle.
 * @author Sarge
 */
class JoystickAxis extends AbstractSource<AxisEvent> implements Axis {
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

	@Override
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
