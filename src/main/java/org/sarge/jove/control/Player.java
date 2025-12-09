package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * A <i>player</i> is an adapter for a {@link Playable} that notifies interested listeners on state transitions.
 * @author Sarge
 */
public class Player implements Playable {
	private final Playable playable;
	private final Collection<Consumer<Player>> listeners = new HashSet<>();

	/**
	 * Constructor.
	 * @param playable Underlying playable
	 */
	public Player(Playable playable) {
		this.playable = requireNonNull(playable);
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
		for(Consumer<Player> listener : listeners) {
			listener.accept(this);
		}
	}

	/**
	 * Adds a state change listener.
	 * @param listener State change listener
	 */
	public void add(Consumer<Player> listener) {
		requireNonNull(listener);
		listeners.add(listener);
	}

	/**
	 * Removes a state change listener.
	 * @param listener Listener to remove
	 */
	public void remove(Consumer<Player> listener) {
		listeners.remove(listener);
	}
}
