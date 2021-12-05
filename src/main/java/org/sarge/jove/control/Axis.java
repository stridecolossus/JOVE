package org.sarge.jove.control;

import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.Source;

/**
 * An <i>axis</i> generates events for an axial controller such as the mouse wheel or a HOTAS gauge.
 * @author Sarge
 */
public interface Axis extends Source<AxisEvent> {
	/**
	 * @return Current axis position
	 */
	float value();

	/**
	 * Handler for an axis event.
	 */
	@FunctionalInterface
	interface Handler {
		/**
		 * Accepts an axis event.
		 * @param value Axis value
		 */
		void handle(float value);
	}

	/**
	 * An <i>axis event</i> describes an event generated by this axis.
	 */
	record AxisEvent(Axis axis, float value) implements Event {
		@Override
		public Object type() {
			return axis;
		}
	}
}
