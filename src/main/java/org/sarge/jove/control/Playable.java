package org.sarge.jove.control;

import org.sarge.jove.control.Player.State;

/**
 * A <i>playable</i> is a media resource controlled by this player.
 */
public interface Playable {
	/**
	 * @return Whether this playable is playing
	 */
	boolean isPlaying();

	/**
	 * Sets the state of this playable.
	 * @param state New state
	 * @throws IllegalStateException for an illegal state transition
	 */
	void state(State state);

	/**
	 * Sets whether this playable should repeat.
	 * @param repeat Whether repeating
	 */
	void repeat(boolean repeat);
}
