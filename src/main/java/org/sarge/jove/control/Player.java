package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Playable.State;

/**
 * A <i>player</i> is a controller for playable media and animations.
 * @author Sarge
 */
public class Player {
	/**
	 * Listener for player state changes.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies a player state change.
		 * @param player Player
		 */
		void update(Player player);
	}

	private final Collection<Playable> playing = new ArrayList<>();
	private final Collection<Listener> listeners = new HashSet<>();

	private State state = State.STOP;

	/**
	 * Adds a playable to this player.
	 * @param playable Playable to add
	 */
	public void add(Playable playable) {
		playing.add(notNull(playable));
	}

	/**
	 * Removes a playable.
	 * @param playable Playable to remove
	 */
	public void remove(Playable playable) {
		playing.remove(playable);
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

	/**
	 * @return State of this player
	 */
	public State state() {
		return state;
	}

	/**
	 * Sets the state of this player.
	 * @param state Player state
	 */
	public void state(State state) {
		// Change state
		this.state.validate(state);
		this.state = notNull(state);

		// Delegate
		for(Playable p : playing) {
			p.state(state);
		}

		// Notify listeners
		for(Listener listener : listeners) {
			listener.update(this);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(state)
				.append("playing", playing.size())
				.append("listeners", listeners.size())
				.build();
	}
}
