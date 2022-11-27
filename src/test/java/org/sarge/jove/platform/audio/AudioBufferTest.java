package org.sarge.jove.platform.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.NativeObject;

class AudioBufferTest {
	private AudioBuffer buffer;
	private AudioDevice dev;
	private AudioLibrary lib;
	private Audio audio;

	@BeforeEach
	void before() {
		lib = mock(AudioLibrary.class);
		dev = mock(AudioDevice.class);
		when(dev.library()).thenReturn(lib);
		buffer = AudioBuffer.create(dev);
		audio = new Audio(2, 8, 42, new byte[100]);
	}

	@Test
	void constructor() {
		assertEquals(false, buffer.isDestroyed());
	}

	@Test
	void load() {
		final byte[] data = audio.data();
		buffer.load(audio);
		verify(lib).alBufferData(buffer, AudioParameter.STEREO_8.value(), data, data.length, 42);
	}

	@Test
	void destroy() {
		buffer.destroy();
		assertEquals(true, buffer.isDestroyed());
		verify(lib).alDeleteBuffers(1, NativeObject.array(List.of(buffer)));
	}
}
