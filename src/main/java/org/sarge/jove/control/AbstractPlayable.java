package org.sarge.jove.control;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractPlayable implements Playable {
	private boolean playing;

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void play() {
		if(playing) throw new IllegalStateException("Already playing: " + this);
		playing = true;
	}

	@Override
	public void pause() {
		if(!playing) throw new IllegalStateException("Not playing: " + this);
		playing = false;
	}

	@Override
	public void stop() {
		playing = false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("playing", playing).build();
	}
}
