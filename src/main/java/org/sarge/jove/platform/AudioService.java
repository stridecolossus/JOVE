package org.sarge.jove.platform;

import org.sarge.jove.control.Player.Playable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * An <i>audio service</i> abstracts the audio playback capabilities of the platform.
 * @author Sarge
 */
public interface AudioService extends Service {
	/**
	 * Floating-point range.
	 */
	public static final class Range extends AbstractEqualsObject {
		private final float min, max;

		/**
		 * Constructor.
		 * @param min Minimum value
		 * @param max Maximum value
		 * @throws IllegalArgumentException if the range is not valid
		 */
		public Range(float min, float max) {
			if(max < min) throw new IllegalArgumentException("Invalid range");
			this.min = min;
			this.max = max;
		}

		/**
		 * @return Get minimum
		 */
		public float min() {
			return min;
		}

		/**
		 * @return Get maximum
		 */
		public float max() {
			return max;
		}

		/**
		 * @param value Value
		 * @return Whether the given value is valid for this range
		 */
		public boolean contains(float value) {
			return (value >= min) && (value <= max);
		}
	}

	/**
	 * An <i>audio buffer</i> contains buffered {@link AudioData} which can be bound to an {@link Source}.
	 */
	interface Buffer extends Resource {
		/**
		 * Loads the given data into this buffer.
		 * @param audio Audio data
		 * @throws UnsupportedOperationException if the audio format is not supported by this platform
		 */
		void load(AudioData audio);
	}

	/**
	 * An <i>audio source</i> is bound to a {@link Buffer} to play audio.
	 */
	interface Source extends Playable, Resource {
		/**
		 * Sets the position of this source.
		 * @param pos Position
		 */
		void position(Point pos);

		/**
		 * Sets the velocity vector of this source.
		 * @param velocity Velocity
		 */
		void velocity(Vector velocity);

		/**
		 * Sets the direction of this source.
		 * @param dir Direction
		 */
		void direction(Vector dir);

		/**
		 * @return Gain range
		 */
		Range gain();

		/**
		 * Sets the gain (volume amplification) of this source.
		 * @param gain Gain
		 * @throws IllegalArgumentException if the given gain is not valid for this source
		 * @see #gain()
		 */
		void gain(float gain);

		/**
		 * @return Pitch range
		 */
		Range pitch();

		/**
		 * Sets the pitch modifier of this source.
		 * @param pitch Pitch modifier
		 * @throws IllegalArgumentException if the given pitch is not valid for this source
		 * @see #pitch()
		 */
		void pitch(float pitch);

		/**
		 * Binds the given buffer to this source.
		 * @param buffer Audio buffer
		 */
		void bind(Buffer buffer);
	}

	/**
	 * The <i>listener</i> is the receiver for audio.
	 */
	interface Listener {
		/**
		 * Sets the position of the listener.
		 * @param pos Position
		 */
		void position(Point pos);

		/**
		 * Sets the listener velocity vector.
		 * @param pos Position
		 */
		void velocity(Vector velocity);

		/**
		 * Sets the listener orientation.
		 * @param view		View direction
		 * @param up		Up direction
		 */
		void orientation(Vector view, Vector up);

		/**
		 * Sets the listener gain.
		 * @param gain Gain
		 */
		void gain(float gain);
	}

	/**
	 * Creates an audio source.
	 * @return New audio source
	 */
	Source source();

	/**
	 * Creates an audio buffer.
	 * @return New audio buffer
	 */
	Buffer buffer();

	/**
	 * @return Audio listener
	 */
	Listener listener();
}
