package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>player</i> is a controller for playable media and animations.
 * @author Sarge
 */
public class Player extends Playable {
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

	private Playable playable;
	private final Collection<Listener> listeners = new HashSet<>();

	/**
	 * Constructor.
	 * @param playable Playable object
	 */
	public Player(Playable playable) {
		set(playable);
	}

	/**
	 * @return Playable object controlled by this player
	 */
	public Playable playable() {
		return playable;
	}

	/**
	 * Sets the playable object being controlled by this player.
	 * @param playable Playable object
	 */
	public void set(Playable playable) {
		this.playable = notNull(playable);
	}

	@Override
	public State state() {
		final State state = super.state();
		if((state == State.PLAY) && !playable.isPlaying()) {
			super.apply(State.STOP);
			update();
			return State.STOP;
		}
		return state;
	}

	@Override
	public void apply(State state) {
		super.apply(state);
		playable.apply(state);
		update();
	}

	/**
	 * Notifies listeners of a state change.
	 */
	private void update() {
		for(Listener listener : listeners) {
			listener.update(this);
		}
	}

	/**
	 * Adds a state change listener.
	 * @param listener State change listener
	 */
	public void add(Listener listener) {
		listeners.add(notNull(listener));
	}

	/**
	 * Removes a state change listener.
	 * @param listener Listener to remove
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(playable)
				.append("listeners", listeners.size())
				.build();
	}
}
