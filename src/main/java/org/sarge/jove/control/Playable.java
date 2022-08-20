package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>playable</i> abstracts media or animations that can be controlled by a {@link Player}.
 * @author Sarge
 */
public abstract class Playable {
	/**
	 * Playable states.
	 */
	public enum State {
		PLAY,
		PAUSE,
		STOP;

		/**
		 * @param next Next state
		 * @throws IllegalStateException for an invalid state transition
		 */
		void validate(State next) {
			if(this == next) {
				throw new IllegalStateException("Duplicate player state: " + this);
			}
			if((next == PAUSE) && (this != PLAY)) {
				throw new IllegalStateException(String.format("Illegal player state transition: prev=%s next=%s", this, next));
			}
		}
	}

	private State state = State.STOP;
	private boolean repeat;

	/**
	 * @return Current state of this playable
	 */
	protected State state() {
		return state;
	}

	/**
	 * @return Whether this playable is currently playing
	 */
	public boolean isPlaying() {
		return state == State.PLAY;
	}

	/**
	 * Sets the state of this playable.
	 * @param state New state
	 * @throws IllegalStateException for an illegal state transition
	 */
	protected void state(State state) {
		this.state = notNull(state);
	}

	/**
	 * @return Whether this playable is repeating
	 */
	public boolean isRepeating() {
		return repeat;
	}

	/**
	 * Sets whether this playable should repeat.
	 * @param repeat Whether repeating
	 */
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(state)
				.append("repeat", repeat)
				.build();
	}

	/**
	 * A <i>playable media</i> is a base-class for a playable resource that may stop in the background.
	 * Note that {@link #isPlaying()} must be implemented to test when the media has finished.
	 */
	public static abstract class Media extends Playable {
		@Override
		protected State state() {
			update();
			return super.state();
		}

		@Override
		protected void state(State state) {
			update();
			super.state(state);
		}

		@Override
		public abstract boolean isPlaying();

		/**
		 * Checks whether this media has stopped playing.
		 */
		private void update() {
			if(super.isPlaying() && !isPlaying()) {
				super.state(State.STOP);
			}
		}
	}
}
