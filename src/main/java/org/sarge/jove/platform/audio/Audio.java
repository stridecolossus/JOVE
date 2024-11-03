package org.sarge.jove.platform.audio;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.io.*;
import java.time.Duration;

import javax.sound.sampled.*;

import org.sarge.jove.io.ResourceLoader;

/**
 * Audio clip.
 * @author Sarge
 */
public record Audio(int channels, int bitsPerSample, int frequency, byte[] data) {
	/**
	 * Number of channels for mono audio.
	 */
	public static final int MONO = 1;

	/**
	 * Number of channels for stereo audio.
	 */
	public static final int STEREO = 2;

	/**
	 * Constructor.
	 * @param channels		Number of channels
	 * @param samples		Bits per sample
	 * @param freq			Frequency (or sample rate)
	 * @param data			Audio data
	 * @throws IllegalArgumentException if {@link #channels} is not {@link #MONO} or {@link #STEREO}
	 * @throws IllegalArgumentException if {@link #samples} is not 8 or 16
	 */
	public Audio {
		requireOneOrMore(frequency);
		requireNonNull(data);
		final int bytes = bitsPerSample / Byte.SIZE;
		if((bytes < 1) || (bytes > 2)) throw new IllegalArgumentException("Bits-per-sample must be 8 or 16");
		if((channels < 1) || (channels > 2)) throw new IllegalArgumentException("Channels must be mono(1) or stereo(2)");
		// TODO - check length
	}

	/**
	 * @return Number of samples
	 */
	public int samples() {
		return data.length / (channels * bitsPerSample / Byte.SIZE);
	}

	/**
	 * @return Duration of this audio
	 */
	public Duration duration() {
		return Duration.ofSeconds(samples() / frequency);
	}

	/**
	 * Loader for an audio clip.
	 */
	public static class Loader implements ResourceLoader<AudioInputStream, Audio> {
		@Override
		public AudioInputStream map(InputStream in) throws Exception {
			return AudioSystem.getAudioInputStream(in);
		}

		@Override
		public Audio load(AudioInputStream in) throws IOException {
    		final AudioFormat format = in.getFormat();
    		final int channels = format.getChannels();
    		final int samples = format.getSampleSizeInBits();
    		final int freq = (int) format.getSampleRate();
    		final byte[] data = in.readAllBytes();
    		return new Audio(channels, samples, freq, data);
		}
	}
}
