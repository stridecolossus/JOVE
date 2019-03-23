package org.sarge.jove.platform.openal;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.AudioData;
import org.sarge.jove.platform.AudioService;

public class OpenAudioServiceTest {
	private OpenAudioService service;
	private OpenAudioLibrary lib;

	@BeforeEach
	public void before() {
		lib = mock(OpenAudioLibrary.class);
		service = new OpenAudioService(lib);
	}

	@Test
	public void constructor() {
		assertEquals("OpenAL", service.name());
		assertNotNull(service.listener());
	}

	@Test
	public void source() {
		final AudioService.Source src = service.source();
		assertNotNull(src);
	}

	@Nested
	class BufferTests {
		@Test
		public void buffer() {
			final AudioService.Buffer buffer = service.buffer();
			assertNotNull(buffer);
			buffer.destroy();
			verify(lib).alDeleteBuffers(1, new int[]{0});
		}

		@Test
		public void bufferLoad() {
			final AudioData.Format format = new AudioData.Format.Builder().channels(1).samples(16).rate(42).build();
			final byte[] data = new byte[]{};
			final AudioData audio = new AudioData(format, data);
			final AudioService.Buffer buffer = service.buffer();
			buffer.load(audio);
			verify(lib).alBufferData(0, 0x1101, data, data.length, 42);
		}
	}

	@Nested
	class ListenerTests {
		private AudioService.Listener listener;

		@BeforeEach
		public void before() {
			listener = service.listener();
		}

		@Test
		public void position() {
			final Point pos = new Point(1, 2, 3);
			listener.position(pos);
			verify(lib).alListener3f(0x1004, 1, 2, 3);
		}

		@Test
		public void velocity() {
			final Vector velocity = new Vector(1, 2, 3);
			listener.velocity(velocity);
			verify(lib).alListener3f(0x1006, 1, 2, 3);
		}

		@Test
		public void orientation() {
			listener.orientation(Vector.Z_AXIS, Vector.Y_AXIS);
			verify(lib).alListenerfv(0x100F, new float[]{0, 0, 1, 0, 1, 0});
		}

		@Test
		public void gain() {
			listener.gain(42);
			verify(lib).alListenerf(0x100A, 42);
		}
	}

	@Test
	public void close() {
		service.close();
		verify(lib).alcCloseDevice(null);
		verify(lib).alcDestroyContext(null);
	}

	@Tag("openAL")
	@Test
	public void create() {
		final OpenAudioService service = OpenAudioService.create();
		service.close();
	}
}
