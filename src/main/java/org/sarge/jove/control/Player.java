package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.HashSet;

import org.sarge.lib.util.AbstractObject;

/**
 * A <i>player</i> is a model for animations, audio, etc. that can be played and paused.
 * @author Sarge
 */
public class Player extends AbstractObject {
	/**
	 * Player states.
	 */
	public enum State {
		PLAY,
		PAUSE,
		STOP
	}

	/**
	 * A <i>playable</i> is something that can be played, paused or stopped.
	 * @author Sarge
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
	 */
	public void set(State state) {
		// Verify state-change
		// TODO - move to enum
		update();
		switch(state) {
		case PLAY:
			if(this.state == State.PLAY) throw new IllegalStateException("Already playing");
			break;

		case STOP:
			if(this.state == State.STOP) throw new IllegalStateException("Not playing");
			break;

		case PAUSE:
			if(this.state != State.PLAY) throw new IllegalStateException("Not playing");
			break;

		default:
			throw new RuntimeException();
		}

		// Update state
		this.state = state;

		// Notify listeners
		listeners.forEach(listener -> listener.update(state));
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
		Check.notNull(listener);
		listeners.add(listener);
	}
}
