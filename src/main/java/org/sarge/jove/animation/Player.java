package org.sarge.jove.animation;

/**
 * Defines something that can be played.
 * @author Sarge
 */
public interface Player {
	/**
	 * Player state.
	 */
	enum State {
		STOPPED,
		PLAYING,
		PAUSED,
	}

	/**
	 * @return Current state of this player
	 */
	State getState();

	/**
	 * Sets the state of this player.
	 * @param state Player state
	 */
	void setState( State state );
}
