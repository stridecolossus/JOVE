package org.sarge.jove.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.AudioData.Format;
import org.sarge.jove.platform.AudioData.Loader;

public class AudioDataTest {
	@Nested
	class AudioDataTests {
		private Format format;

		@BeforeEach
		public void before() {
			format = new Format.Builder()
				.channels(1)
				.samples(16)
				.rate(44100)
				.build();
		}

		@Test
		public void constructor() {
			final byte[] data = new byte[]{};
			final AudioData audio = new AudioData(format, data);
			assertEquals(format, audio.format());
			assertEquals(data, audio.data());
		}

		@Test
		public void format() {
			assertEquals(1, format.channels());
			assertEquals(16, format.samples());
			assertEquals(44100, format.rate());
		}
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		public void before() {
			loader = new Loader();
		}

		// TODO - parameterize for multiple audio formats
		@Test
		public void load() throws Exception {
			// Load audio
			final AudioData audio = loader.load(AudioDataTest.class.getClassLoader().getResourceAsStream("Footsteps.wav"));
			assertNotNull(audio);
			assertNotNull(audio.data());

			// Check format
			final Format expected = new Format.Builder()
				.channels(1)
				.samples(16)
				.rate(44100)
				.build();
			assertEquals(expected, audio.format());
		}
	}
}
