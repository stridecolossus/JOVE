package org.sarge.jove.control;

/**
 * A <i>playable</i> is a media file or animation that can be controlled by a {@link Player}.
 * @author Sarge
 */
public interface Playable {
	/**
	 * @return Whether this playable is currently playing
	 */
	boolean isPlaying();

	/**
	 * Plays or un-pauses this playable.
	 * @throws IllegalStateException if already playing
	 */
	void play();

	/**
	 * Stops this playable.
	 * @throws IllegalStateException if not playing
	 */
	void stop();

	/**
	 * Pauses this playable.
	 * @throws IllegalStateException if not playing
	 */
	default void pause() {
		stop();
	}
}
