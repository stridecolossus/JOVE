package org.sarge.jove.audio;

import org.sarge.jove.common.GraphicResource;

/**
 * An audio track is a <i>buffer</i> for an {@link AudioData} and is played by an {@link AudioPlayer}.
 */
public interface AudioTrack extends GraphicResource {
	/**
	 * Buffers the given audio data into this track.
	 * @param data Audio data
	 */
	void buffer( AudioData data );
}
