package org.sarge.jove.control;

/**
 * A <i>playable</i> is a media file or animation that can be controlled by a {@link Player}.
 * @author Sarge
 */
public interface Playable {
	/**
	 * Playable states.
	 */
	enum State {
		PLAY,
		PAUSE,
		STOP
	}

	/**
	 * @return Whether this playable is currently playing
	 */
	boolean isPlaying();

	/**
	 * Sets the state of this playable.
	 * @param state New state
	 * @throws IllegalStateException for an illegal state transition
	 */
	void state(State state);
}
