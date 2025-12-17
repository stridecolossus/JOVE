package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractPlayable implements Playable {
	private State state = State.STOPPED;

	@Override
	public State state() {
		return state;
	}

	/**
	 * @return Whether currently playing
	 */
	public boolean isPlaying() {
		return state == State.PLAYING;
	}

	@Override
	public void state(State state) {
		if(state == this.state) {
			throw new IllegalStateException("Duplicate state %s for %s".formatted(state, this));
		}

		if((state == State.PAUSED) && (this.state != State.PLAYING)) {
			throw new IllegalStateException("Not playing: " + this);
		}

		this.state = requireNonNull(state);
	}

	@Override
	public String toString() {
		return String.format("Playable[%s]", state);
	}
}
