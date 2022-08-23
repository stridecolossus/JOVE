package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractPlayable implements Playable {
	private State state = State.STOP;

	@Override
	public void state(State state) {
		this.state = notNull(state);
	}

	@Override
	public boolean isPlaying() {
		return state == State.PLAY;
	}

	@Override
	public boolean isRepeating() {
		return false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(state)
				.append("repeat", isRepeating())
				.build();
	}
}
