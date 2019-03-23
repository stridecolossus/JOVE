package org.sarge.jove.platform;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Audio data.
 * TODO - extend for streaming
 */
public final class AudioData {
	/**
	 * Audio format descriptor.
	 */
	public static final class Format extends AbstractEqualsObject {
		private final int channels;
		private final int samples;
		private final int rate;

		/**
		 * Constructor.
		 * @param channels		Number of channels
		 * @param samples		Sample size (bits)
		 * @param rate			Sampling rate
		 */
		public Format(int channels, int samples, int rate) {
			this.channels = oneOrMore(channels);
			this.samples = oneOrMore(samples);
			this.rate = oneOrMore(rate);
		}

		/**
		 * @return Number of channels
		 */
		public int channels() {
			return channels;
		}

		/**
		 * @return Sample size (bits)
		 */
		public int samples() {
			return samples;
		}

		/**
		 * @return Sample rate
		 */
		public int rate() {
			return rate;
		}

		/**
		 * Builder for a format descriptor.
		 */
		public static class Builder {
			private int channels = 1;
			private int samples = 8;
			private int rate;

			/**
			 * Sets the number of channels.
			 * @param channels Number of channels
			 */
			public Builder channels(int channels) {
				this.channels = channels;
				return this;
			}

			/**
			 * Sets the samples size.
			 * @param samples Sample size (bits)
			 */
			public Builder samples(int samples) {
				this.samples = samples;
				return this;
			}

			/**
			 * Sets the sample rate.
			 * @param rate Sample rate
			 */
			public Builder rate(int rate) {
				this.rate = rate;
				return this;
			}

			/**
			 * Constructs this format descriptor.
			 * @return New format descriptor
			 */
			public Format build() {
				return new Format(channels, samples, rate);
			}
		}
	}

	private final Format format;
	private final byte[] data;

	/**
	 * Constructor.
	 * @param format		Audio format descriptor
	 * @param data			Audio data
	 */
	public AudioData(Format format, byte[] data) {
		this.format = notNull(format);
		this.data = notNull(data);
	}

	/**
	 * @return Format descriptor for this audio
	 */
	public Format format() {
		return format;
	}

	/**
	 * @return Audio data
	 */
	public byte[] data() {
		return data;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("format", format)
			.append("size", data.length)
			.toString();
	}

	/**
	 * Loader for audio data.
	 * TODO - this should be a platform specific service
	 */
	public static class Loader {
		/**
		 * Loads an audio file.
		 * @param in Input stream
		 * @return Audio data
		 * @throws UnsupportedAudioFileException if the file is not supported
		 * @throws IOException if the file cannot be loaded
		 */
		public AudioData load(InputStream in) throws UnsupportedAudioFileException, IOException {
			// Load audio file
			final byte[] array;
			final AudioFormat format;
			try(final AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(in))) {
				array = stream.readAllBytes();
				format = stream.getFormat();
			}

			// Create audio descriptor
			final Format descriptor = new Format.Builder()
				.channels(format.getChannels())
				.samples(format.getSampleSizeInBits())
				.rate((int) format.getSampleRate())
				.build();

			// Create wrapper
			return new AudioData(descriptor, array);
		}
	}
}
