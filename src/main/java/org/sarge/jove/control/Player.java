package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>player</i> is a controller for playable media and animations.
 * @author Sarge
 */
public class Player extends AbstractPlayable {
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

	@Override
	public void play() {
		super.play();
		delegate(Playable::play);
	}

	@Override
	public void pause() {
		super.pause();
		delegate(Playable::pause);
	}

	@Override
	public void stop() {
		super.stop();
		delegate(Playable::stop);
	}

	private void delegate(Consumer<Playable> state) {
		playing.forEach(state);
		listeners.forEach(e -> e.update(this));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("count", playing.size())
				.append("listeners", listeners.size())
				.build();
	}
}
