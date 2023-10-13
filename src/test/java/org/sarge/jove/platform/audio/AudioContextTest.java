package org.sarge.jove.platform.audio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.sarge.jove.common.Handle;

class AudioContextTest {
	private AudioContext ctx;
	private AudioDevice dev;
	private AudioLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(AudioLibrary.class);
		dev = mock(AudioDevice.class);
		when(dev.library()).thenReturn(lib);
		when(lib.alcCreateContext(dev, new int[0])).thenReturn(new Handle(1));
		ctx = AudioContext.create(dev);
	}

	@Test
	void constructor() {
		assertEquals(dev, ctx.device());
		assertEquals(false, ctx.isDestroyed());
	}

	@Test
	void failed() {
		Mockito.when(lib.alcCreateContext(dev, new int[0])).thenReturn(null);
		assertThrows(RuntimeException.class, () -> AudioContext.create(dev));
	}

	@Test
	void current() {
		ctx.setCurrent();
		verify(lib).alcMakeContextCurrent(ctx);
	}

	@Test
	void destroy() {
		ctx.destroy();
		assertEquals(true, ctx.isDestroyed());
		verify(lib).alcDestroyContext(ctx);
	}
}
