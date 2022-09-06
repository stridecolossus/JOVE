package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.*;

public class AbstractPlayableTest {
	private AbstractPlayable playable;

	@BeforeEach
	void before() {
		playable = spy(AbstractPlayable.class);
	}

	@DisplayName("A new playable is initially stopped")
	@Test
	void constructor() {
		assertEquals(false, playable.isPlaying());
	}

	@DisplayName("A playable that is stopped...")
	@Nested
	class Stopped {
		@DisplayName("can be played")
		@Test
		void play() {
			playable.play();
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.pause());
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.stop());
		}
	}

	@DisplayName("A playable that is playing...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			playable.play();
		}

		@DisplayName("cannot be played")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> playable.play());
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			playable.pause();
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.stop();
			assertEquals(false, playable.isPlaying());
		}
	}

	@DisplayName("A playable that is paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			playable.play();
			playable.pause();
		}

		@DisplayName("can be restarted")
		@Test
		void play() {
			playable.play();
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.pause());
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.stop());
		}
	}
}
