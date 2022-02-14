package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>player</i> is a controller for media and animations.
 * @author Sarge
 */
public class Player implements Playable {
	/**
	 * Playable states.
	 */
	enum State {
		PLAY,
		PAUSE,
		STOP;

		private void validate(State prev) {
			if(this == prev) {
				throw new IllegalStateException("Duplicate player state: " + this);
			}
			if((this == PAUSE) && (prev != PLAY)) {
				throw new IllegalStateException(String.format("Illegal player state transition: prev=%s next=%s", prev, this));
			}
		}
	}

	/**
	 * Listener for player state changes.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies a player state change.
		 * @param state New state
		 */
		void update(State state);
	}

	private final Collection<Listener> listeners = new HashSet<>();
	private State state = State.STOP;
	private boolean repeat;

	@Override
	public boolean isPlaying() {
		return state == State.PLAY;
	}

	/**
	 * @return Current state of this player
	 */
	public State state() {
		return state;
	}

	@Override
	public void state(State state) {
		// Update state
		state.validate(this.state);
		this.state = notNull(state);

		// Notify listeners
		for(Listener listener : listeners) {
			listener.update(state);
		}
	}

	/**
	 * @return Whether this player is repeating
	 */
	public boolean isRepeating() {
		return repeat;
	}

	@Override
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	/**
	 * Adds a player state change listener.
	 * @param listener State change listener
	 */
	public void add(Listener listener) {
		listeners.add(notNull(listener));
	}

	/**
	 * Removes a listener.
	 * @param listener Listener to remove
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("state", state)
				.append("repeating", repeat)
				.append("listeners", listeners.size())
				.build();
	}
}
