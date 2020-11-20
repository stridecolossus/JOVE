package org.sarge.jove.control;

/**
 * An <i>action</i> defines a method handler for an event.
 * @author Sarge
 */
public interface Action<T extends InputEvent.Type> {
	/**
	 * Action for a button.
	 */
	interface SimpleAction extends Action<Button>, Runnable {
		// Marker interface
	}

	/**
	 * Action for a positional event.
	 */
	interface PositionAction extends Action<Position> {
		/**
		 * Handles a position event.
		 * @param x
		 * @param y
		 */
		void handle(float x, float y);
	}

	/**
	 * Action for an axial event.
	 */
	interface ValueAction extends Action<Axis> {
		/**
		 * Handles an axis event.
		 * @param value Axis value
		 */
		void handle(float value);
	}
}
