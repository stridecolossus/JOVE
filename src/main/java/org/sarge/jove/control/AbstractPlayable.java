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
	public State state() {
		return state;
	}

	@Override
	public void apply(State state) {
		if(!this.state.isValidTransition(state)) throw new IllegalStateException("Invalid state transition: this=%s next=%s".formatted(this, state));
		this.state = notNull(state);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(state).build();
	}
}
