package org.sarge.jove.platform.audio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.control.Playable.State;
import org.sarge.jove.geometry.*;
import org.sarge.jove.platform.audio.AudioSource.AudioSourcePlayable;

import com.sun.jna.ptr.IntByReference;

class AudioSourceTest {
	private AudioSource source;
	private AudioDevice dev;
	private AudioLibrary lib;
	private AudioBuffer buffer;
	private AudioSourcePlayable playable;

	@BeforeEach
	void before() {
		lib = mock(AudioLibrary.class);
		dev = mock(AudioDevice.class);
		when(dev.library()).thenReturn(lib);
		source = AudioSource.create(dev);
		buffer = mock(AudioBuffer.class);
		playable = source.playable();
	}

	@Test
	void constructor() {
		assertEquals(false, source.isDestroyed());
		assertEquals(dev, source.device());
	}

	@Test
	void pitch() {
		source.pitch(2);
		verify(lib).alSourcef(source, AudioParameter.PITCH, 2f);
	}

	@Test
	void gain() {
		source.gain(2);
		verify(lib).alSourcef(source, AudioParameter.GAIN, 2f);
	}

	@Test
	void loop() {
		source.loop(true);
		verify(lib).alSourcei(source, AudioParameter.LOOPING, true);
	}

	@Test
	void position() {
		final Point pos = new Point(1, 2, 3);
		source.position(pos);
		verify(lib).alSource3f(source, AudioParameter.POSITION, 1, 2, 3);
	}

	@Test
	void direction() {
		final Vector dir = new Vector(1, 2, 3);
		source.direction(dir);
		verify(lib).alSource3f(source, AudioParameter.DIRECTION, 1, 2, 3);
	}

	@Test
	void velocity() {
		final Vector velocity = new Vector(1, 2, 3);
		source.velocity(velocity);
		verify(lib).alSource3f(source, AudioParameter.VELOCITY, 1, 2, 3);
	}

	@Test
	void buffer() {
		source.buffer(buffer);
		verify(lib).alSourcei(source, AudioParameter.BUFFER, buffer);
	}

	@DisplayName("An empty audio source...")
	@Nested
	class Empty {
		@DisplayName("does not have a buffer")
		@Test
		void empty() {
			assertEquals(0, source.buffers().count());
		}

		@DisplayName("can contain an audio buffer")
		@Test
		void buffer() {
			source.buffer(buffer);
			assertEquals(List.of(buffer), source.buffers().toList());
		}

		@DisplayName("cannot be played")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> playable.apply(State.PLAY));
		}
	}

	@DisplayName("An audio source with a buffer...")
	@Nested
	class New {
		@BeforeEach
		void before() {
			source.buffer(buffer);
		}

		@DisplayName("is not playing")
		@Test
		void isPlaying() {
			assertEquals(false, playable.isPlaying());
		}

		@Test
		void clear() {
			source.clear();
			assertEquals(0, source.buffers().count());
			verify(lib).alSourcei(source, AudioParameter.BUFFER, 0);
		}

		@DisplayName("can be played")
		@Test
		void play() {
			playable.apply(State.PLAY);
			verify(lib).alSourcePlay(source);
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.apply(State.PAUSE));
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.apply(State.STOP));
		}

		@DisplayName("can be rewound")
		@Test
		void rewind() {
			source.rewind();
			verify(lib).alSourceRewind(source);
		}
	}

	@DisplayName("An audio source that is being played...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			source.buffer(buffer);
			playable.apply(State.PLAY);
		}

		@DisplayName("checks whether the audio has finished")
		@Test
		void isPlaying() {
			final Answer<Void> answer = inv -> {
				final IntByReference ref = inv.getArgument(2);
				ref.setValue(AudioParameter.PLAYING.value());
				return null;
			};
			doAnswer(answer).when(lib).alGetSourcei(eq(source), eq(AudioParameter.SOURCE_STATE), any(IntByReference.class));
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("is stopped when the audio finishes")
		@Test
		void finished() {
			assertEquals(false, playable.isPlaying());
			verify(lib).alGetSourcei(eq(source), eq(AudioParameter.SOURCE_STATE), any(IntByReference.class));
		}

		@DisplayName("cannot be played")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> playable.apply(State.PLAY));
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			playable.apply(State.PAUSE);
			assertEquals(false, playable.isPlaying());
			verify(lib).alSourcePause(source);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.apply(State.STOP);
			assertEquals(false, playable.isPlaying());
			verify(lib).alSourceStop(source);
		}
	}

	@DisplayName("An audio source that has been paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			source.buffer(buffer);
			playable.apply(State.PLAY);
			playable.apply(State.PAUSE);
		}

		@DisplayName("is not playing")
		@Test
		void isPlaying() {
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be restarted")
		@Test
		void play() {
			playable.apply(State.PLAY);
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.apply(State.PAUSE));
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.apply(State.STOP);
		}
	}

	@Test
	void destroy() {
		source.destroy();
		assertEquals(true, source.isDestroyed());
		verify(lib).alDeleteSources(1, NativeObject.array(List.of(source)));
	}
}
