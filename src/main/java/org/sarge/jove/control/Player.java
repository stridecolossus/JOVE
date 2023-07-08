package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>player</i> is an adapter for a {@link Playable} that notifies interested listeners on state transitions.
 * @author Sarge
 */
public class Player implements Playable {
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

	private final Playable playable;
	private final Collection<Listener> listeners = new HashSet<>();

	/**
	 * Constructor.
	 * @param playable Underlying playable
	 */
	public Player(Playable playable) {
		this.playable = notNull(playable);
	}

	/**
	 * @return Underlying playable
	 */
	public Playable playable() {
		return playable;
	}

	@Override
	public boolean isPlaying() {
		return playable.isPlaying();
	}

	@Override
	public void play() {
		playable.play();
		update();
	}

	@Override
	public void pause() {
		playable.pause();
		update();
	}

	@Override
	public void stop() {
		playable.stop();
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
