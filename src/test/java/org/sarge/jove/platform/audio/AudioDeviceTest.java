package org.sarge.jove.platform.audio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.sarge.jove.common.Handle;

class AudioDeviceTest {
	private AudioDevice dev;
	private AudioLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(AudioLibrary.class);
		when(lib.alcOpenDevice(null)).thenReturn(new Handle(1));
		dev = AudioDevice.create(null, lib);
	}

	@Test
	void constructor() {
		assertEquals(lib, dev.library());
		assertEquals(false, dev.isDestroyed());
	}

	@Test
	void failed() {
		Mockito.when(lib.alcOpenDevice(null)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> AudioDevice.create(null, lib));
	}

	@Test
	void devices() {
		Mockito.when(lib.alcGetString(null, AudioParameter.DEVICE_SPECIFIER)).thenReturn("one" + (char) 0 + "two");
		when(lib.alcOpenDevice(anyString())).thenReturn(new Handle(1));
		assertEquals(2, AudioDevice.devices(lib).size());
	}

	@Test
	void none() {
		assertEquals(Optional.empty(), dev.error());
	}

	@Test
	void error() {
		final var error = AudioParameter.INVALID_OPERATION;
		when(lib.alGetError()).thenReturn(error.value());
		assertEquals(Optional.of(error.name()), dev.error());
	}

	@Test
	void check() {
		Mockito.when(lib.alGetError()).thenReturn(AudioParameter.INVALID_OPERATION.value());
		assertThrows(RuntimeException.class, () -> dev.check());
	}

	@Test
	void destroy() {
		Mockito.when(lib.alcCloseDevice(dev)).thenReturn(true);
		dev.destroy();
		assertEquals(true, dev.isDestroyed());
	}
}
