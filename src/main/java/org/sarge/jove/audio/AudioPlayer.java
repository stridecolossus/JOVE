package org.sarge.jove.audio;

import org.sarge.jove.animation.Player;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Audio player.
 */
public interface AudioPlayer extends Player {
	/**
	 * Attaches an audio track to this player.
	 * @param track
	 */
	void bind( AudioTrack track );

	/**
	 * Sets the player position.
	 * @param pos
	 */
	void setPosition( Point pos );

	/**
	 * Sets the player velocity.
	 * @param v
	 */
	void setVelocity( Vector v );

	/**
	 * Sets the audio pitch.
	 * @param pitch
	 */
	void setPitch( float pitch );

	/**
	 * Sets the audio volume.
	 * @param gain
	 */
	void setVolume( float gain );

	/**
	 * Sets whether to auto-repeat.
	 * @param repeat
	 */
	void setRepeat( boolean repeat );
}
