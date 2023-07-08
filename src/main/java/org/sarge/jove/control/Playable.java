package org.sarge.jove.control;

/**
 * A <i>playable</i> object is a media file or animation that can be played, paused and stopped.
 * @author Sarge
 */
public interface Playable {
	/**
	 * @return Whether this object is currently playing
	 */
	boolean isPlaying();

	/**
	 * Starts playing.
	 * @throws IllegalStateException if already playing
	 */
	void play();

	/**
	 * Pauses playing.
	 * @throws IllegalStateException if not playing
	 */
	void pause();

	/**
	 * Stops playing.
	 */
	void stop();
}
