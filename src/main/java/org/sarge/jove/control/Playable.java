package org.sarge.jove.control;

/**
 * A <i>playable</i> object is a media file or animation that can be played, paused and stopped.
 * @author Sarge
 */
public interface Playable {
	/**
	 * State of this playable.
	 */
	enum State {
		STOPPED,
		PLAYING,
		PAUSED
	}

	/**
	 * @return Current state
	 */
	State state();

	/**
	 * Sets the state of this playable.
	 * @param state Next state
	 * @throws IllegalStateException for an invalid state transition
	 */
	void state(State state);
}
