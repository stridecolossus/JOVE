package org.sarge.jove.control;

/**
 * A <i>playable</i> object is a media file or animation that can be controlled by a {@link Player}.
 * @author Sarge
 */
public interface Playable {
	/**
	 * Playable states/operations.
	 */
	enum State {
		STOP,
		PLAY,
		PAUSE;

		/**
		 * Validates a playable state transition.
		 * @param next Next state
		 * @return Whether {@link #next} is a valid transition from this state
		 */
		public boolean isValidTransition(State next) {
			if(this == next) return false;
			if((next == PAUSE) && (this != PLAY)) return false;
			return true;
		}
	}

	/**
	 * @return Current state of this playable object
	 */
	State state();

	/**
	 * @return Whether this playable object is currently playing
	 */
	default boolean isPlaying() {
		return state() == State.PLAY;
	}

	/**
	 * Sets the state of this playable.
	 * @param state New state
	 * @throws IllegalStateException if {@link #state} is invalid for this playable
	 * @see State#isValidTransition(State)
	 */
	void apply(State state);
}
