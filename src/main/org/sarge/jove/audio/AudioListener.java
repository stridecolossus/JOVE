package org.sarge.jove.audio;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Audio service/listener.
 */
public interface AudioListener {
	/**
	 * Initialises the audio system.
	 */
	void start();

	/**
	 * Closes the audio system.
	 */
	void stop();

	/**
	 * Sets the listener ear position.
	 * @param pos Listener position
	 */
	void setPosition( Point pos );

	/**
	 * Sets the listeners velocity.
	 * @param v Listener velocity
	 */
	void setVelocity( Vector v );

	/**
	 * Sets the listeners orientation.
	 * @param dir	Direction
	 * @param up	Up direction
	 */
	void setOrientation( Vector dir, Vector up );

	/**
	 * Creates an audio track.
	 * @return Audio track
	 */
	AudioTrack createAudioTrack();

	/**
	 * Creates an audio player.
	 * @return Audio player
	 */
	AudioPlayer createAudioPlayer();
}
