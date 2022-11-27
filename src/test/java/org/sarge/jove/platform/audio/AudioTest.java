package org.sarge.jove.platform.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;

class AudioTest {
	private Audio audio;

	@BeforeEach
	void before() {
		audio = new Audio(2, 16, 44100, new byte[546872]);
	}

	@Test
	void constructor() {
		assertEquals(2, audio.channels());
		assertEquals(16, audio.bitsPerSample());
		assertEquals(44100, audio.frequency());
		assertNotNull(audio.data());
	}

	@Test
	void samples() {
		assertEquals(136718, audio.samples());
	}

	@Test
	void duration() {
		assertEquals(Duration.ofSeconds(3), audio.duration());
	}

	@Test
	void equals() {
		assertEquals(audio, audio);
		assertNotEquals(audio, null);
		assertNotEquals(audio, new Audio(1, 8, 42, new byte[0]));
	}
}
