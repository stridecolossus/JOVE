package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>player</i> is a model for animations, audio, etc. that can be played and paused.
 * @author Sarge
 */
public class Player {
	/**
	 * Player states.
	 */
	public enum State {
		PLAY,
		PAUSE,
		STOP;

		private void validate(State next) {
			if(this == next) {
				throw new IllegalStateException("Duplicate player state: " + this);
			}
			if((next == PAUSE) && (this != PLAY)) {
				throw new IllegalStateException(String.format("Illegal player state transition: prev=%s next=%s", this, next));
			}
		}
	}

	/**
	 * A <i>playable</i> is a resource that can be played, paused or stopped.
	 */
	public interface Playable {
		/**
		 * Sets the state of this playable.
		 * @param state New state
		 */
		void apply(State state);

		/**
		 * @return Whether this playable is playing
		 */
		boolean isPlaying();

		/**
		 * Sets whether this playable should repeat.
		 * @param repeat Whether repeating
		 */
		void setRepeating(boolean repeat);
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
	private final Playable playable;

	private State state = State.STOP;
	private boolean repeating;

	/**
	 * Constructor.
	 * @param playable Playable object
	 */
	public Player(Playable playable) {
		this.playable = notNull(playable);
	}

	/**
	 * Checks that the playable resource is still playing.
	 */
	private void update() {
		if((state == State.PLAY) && !playable.isPlaying()) {
			state = State.STOP;
		}
	}

	/**
	 * @return Player state
	 */
	public State state() {
		update();
		return state;
	}

	/**
	 * @return Whether currently playing
	 */
	public boolean isPlaying() {
		update();
		return state == State.PLAY;
	}

	/**
	 * Sets the state of this player.
	 * @param state New state
	 * @throws IllegalStateException for an invalid state transition
	 */
	public void set(State state) {
		// Update state
		update();
		this.state.validate(state);
		this.state = state;

		// Notify listeners
		for(Listener listener : listeners) {
			listener.update(state);
		}
	}

	/**
	 * @return Whether this player is repeating
	 */
	public boolean isRepeating() {
		return repeating;
	}

	/**
	 * Sets whether this player is repeating.
	 * @param repeating Whether repeating
	 */
	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
		playable.setRepeating(repeating);
	}

	/**
	 * Adds a player state-change listener.
	 * @param listener State-change listener
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
				.append("repeat", repeating)
				.append("playable", playable)
				.append("listeners", listeners.size())
				.build();
	}
}
