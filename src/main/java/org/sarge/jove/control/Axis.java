package org.sarge.jove.control;

import java.util.function.Consumer;

import org.sarge.jove.control.Event.Source;

/**
 * An <i>axis</i> generates events for an axial controller such as the mouse wheel or a HOTAS gauge.
 * TODO - axis is also its event, caches current value
 * TODO - differentiate between absolute (gauge) and incremental (mouse wheel)
 * TODO - Q, does only absolute have a current value?
 * @author Sarge
 */
public interface Axis extends Event, Source<Axis> {
	/**
	 * @return Current axis position
	 */
	float value();

	/**
	 * An <i>axis handler</i> abstracts a method that can consume an axis event.
	 */
	@FunctionalInterface
	interface Handler {
		/**
		 * Handles a axis event.
		 * @param value Axis value
		 */
		void handle(float value);

		/**
		 * Creates an adapter for an event consumer that delegates to the given axis handler.
		 * @param handler Axis handler
		 * @return Axis event consumer adapter
		 */
		static Consumer<Axis> adapter(Handler handler) {
			return axis -> handler.handle(axis.value());
		}
	}
}
