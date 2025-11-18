package org.sarge.jove.control;

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
		if(playing) {
			throw new IllegalStateException("Already playing: " + this);
		}
		playing = true;
	}

	@Override
	public void pause() {
		if(!playing) {
			throw new IllegalStateException("Not playing: " + this);
		}
		playing = false;
	}

	@Override
	public void stop() {
		playing = false;
	}
}
