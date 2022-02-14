package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>media player</i> is a controller for {@link Playable} media such as an audio file.
 * @author Sarge
 */
public class MediaPlayer extends Player {
	private final Playable playable;

	/**
	 * Constructor.
	 * @param playable Playable media
	 */
	public MediaPlayer(Playable playable) {
		this.playable = notNull(playable);
	}

	/**
	 * Checks that the playable media is still playing.
	 */
	private void update() {
		if(isPlaying() && !playable.isPlaying()) {
			super.state(State.STOP);
		}
	}

	@Override
	public State state() {
		update();
		return super.state();
	}

	@Override
	public void state(State state) {
		update();
		playable.state(state);
		super.state(state);
	}

	@Override
	public void repeat(boolean repeat) {
		playable.repeat(repeat);
		super.repeat(repeat);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("playable", playable)
				.build();
	}
}
