package org.sarge.jove.control;

/**
 * An <i>action</i> defines a method handler for an event.
 * @author Sarge
 */
public interface Action {
	/**
	 * Action for a button.
	 */
	interface SimpleAction extends Action, Runnable {
		// Marker interface
	}

	/**
	 * Action for a positional event.
	 */
	interface PositionAction extends Action {
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
	interface ValueAction extends Action {
		/**
		 * Handles an axis event.
		 * @param value Axis value
		 */
		void handle(float value);
	}
}
