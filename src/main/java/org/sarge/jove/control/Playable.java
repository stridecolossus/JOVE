package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>playable</i> object is a media file or animation that can be controlled by a {@link Player}.
 * @author Sarge
 */
public class Playable {
	/**
	 * Playable states/operations.
	 */
	public enum State {
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

	private State state = State.STOP;

	/**
	 * @return Current state of this playable object
	 */
	public State state() {
		return state;
	}

	/**
	 * @return Whether this playable object is currently playing
	 */
	public boolean isPlaying() {
		return state == State.PLAY;
	}

	/**
	 * Sets the state of this playable.
	 * @param state New state
	 * @throws IllegalStateException if {@link #state} is invalid for this playable
	 * @see State#isValidTransition(State)
	 */
	public void apply(State state) {
		if(!this.state.isValidTransition(state)) throw new IllegalStateException("Invalid state transition: this=%s next=%s".formatted(this, state));
		this.state = notNull(state);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(state).build();
	}
}
