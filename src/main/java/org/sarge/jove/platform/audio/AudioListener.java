package org.sarge.jove.platform.audio;

import static org.sarge.jove.platform.audio.AudioParameter.*;
import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.PointerToFloatArray;
import static org.sarge.lib.Validation.*;

import com.sun.jna.Pointer;

/**
 * The <i>audio listener</i> is used to configure the properties of the audio output device.
 * @author Sarge
 */
public class AudioListener {
	private final AudioDevice dev;
	private final Library lib;

	/**
	 * Constructor.
	 * @param dev Audio device
	 */
	public AudioListener(AudioDevice dev) {
		this.dev = requireNonNull(dev);
		this.lib = dev.library();
	}

	/**
	 * Sets the position of the listener.
	 * @param pos Position
	 */
	public void position(Point pos) {
		lib.alListener3f(POSITION, pos.x, pos.y, pos.z);
		dev.check();
	}

	/**
	 * Sets the velocity of the listener.
	 * @param velocity Velocity
	 */
	public void velocity(Vector velocity) {
		lib.alListener3f(VELOCITY, velocity.x, velocity.y, velocity.z);
		dev.check();
	}

	/**
	 * Sets the orientation of the listener.
	 * @param forward		Forward vector
	 * @param up			Up vector
	 */
	public void orientation(Vector forward, Vector up) {
		final float[] array = {forward.x, forward.y, forward.z, up.x, up.y, up.z};
		lib.alListenerfv(ORIENTATION, new PointerToFloatArray(array));
		dev.check();
	}

	/**
	 * Sets the gain (or volume) of the listener.
	 * @param gain Gain
	 */
	public void gain(float gain) {
		requireZeroOrMore(gain);
		lib.alListenerf(GAIN, gain);
		dev.check();
	}

	/**
	 * Sets the global doppler shift factor (default is one).
	 * @param factor Doppler shift factor
	 */
	public void dopplerFactor(float factor) {
		requireZeroOrMore(factor);
		lib.alDopplerFactor(factor);
		dev.check();
	}

	/**
	 * Sets the global doppler shift velocity (default is one).
	 * @param velocity Doppler velocity
	 */
	public void dopplerVelocity(float velocity) {
		requireZeroOrMore(velocity);
		lib.alDopplerVelocity(velocity);
		dev.check();
	}

	/**
	 * Sets the global speed-of-sound (default is {@code 343.3} m/s).
	 * @param speed Speed-of-sound (metres-per-second)
	 */
	public void speed(float speed) {
		requireZeroOrMore(speed);
		lib.alSpeedOfSound(speed);
		dev.check();
	}

	/**
	 * Distance models.
	 */
	public enum DistanceModel {
		NONE(0, 0),
		INVERSE(0xD001, 0xD002),
		LINEAR(0xD003, 0xD004),
		EXPONENT(0xD005, 0xD006);

		private final int value, clamped;

		private DistanceModel(int value, int clamped) {
			this.value = value;
			this.clamped = clamped;
		}
	}

	/**
	 * Sets the global distance model (default is {@link DistanceModel#INVERSE} clamped).
	 * @param model			Distance model
	 * @param clamped		Whether clamped
	 */
	public void model(DistanceModel model, boolean clamped) {
		final int value = clamped ? model.clamped : model.value;
		lib.alDistanceModel(value);
		dev.check();
	}

	/**
	 * Open AL listener library.
	 */
	interface Library {
		void alListeneri(AudioParameter param, int value);
		void alListenerf(AudioParameter param, float value);
		void alListener3f(AudioParameter param, float value1, float value2, float value3);
		void alListenerfv(AudioParameter param, Pointer values);
		void alDopplerFactor(float dopplerFactor);
		void alDopplerVelocity(float velocity);
		void alSpeedOfSound(float speed);
		void alDistanceModel(int model);
	}
}
