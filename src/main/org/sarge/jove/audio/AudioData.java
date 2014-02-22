package org.sarge.jove.audio;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * In-memory audio chunk.
 * @author Sarge
 */
public class AudioData {
	private int format;
	private int rate;
	private byte[] data;

	/**
	 * Constructor.
	 * @param format	Audio format
	 * @param rate		Sample rate
	 * @param data		Audio data
	 */
	private AudioData( int format, int rate, byte[] data ) {
		Check.notNull( data );
		this.format = format;
		this.rate = rate;
		this.data = data;
	}

	public int getFormat() {
		return format;
	}

	public int getSampleRate() {
		return rate;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
