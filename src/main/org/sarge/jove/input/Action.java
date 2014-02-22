package org.sarge.jove.input;

/**
 * Controller for handling an {@link InputEvent}.
 * @see ActionBindings
 * @author Sarge
 */
public interface Action {
	/**
	 * Performs this action.
	 * @param event Input event
	 */
	void execute( InputEvent event );
}
