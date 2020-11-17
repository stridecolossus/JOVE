package org.sarge.jove.control;

/**
 * Handler for the various types of event.
 * @see InputEvent
 * @author Sarge
 */
public interface Handler {
	/**
	 * Handles an axial event.
	 * @param e Axis event
	 */
	void handle(Axis.Event e);

	/**
	 * Handles a positional event.
	 * @param e Position event
	 */
	void handle(Position.Event e);

	/**
	 * Handles a button event.
	 * @param button Button
	 */
	void handle(Button button);
}
