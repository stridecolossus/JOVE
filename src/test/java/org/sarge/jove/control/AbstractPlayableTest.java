package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class AbstractPlayableTest {
	private AbstractPlayable playable;

	@BeforeEach
	void before() {
		playable = new AbstractPlayable() {
			// Empty
		};
	}

	@DisplayName("A new playable...")
	@Nested
	class Stopped {
		@DisplayName("is initially not playing")
		@Test
		void stopped() {
			assertEquals(false, playable.isPlaying());
		}

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

	@DisplayName("A playable that has been started...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			playable.play();
		}

		@DisplayName("is playing")
		@Test
		void stopped() {
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be played again")
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

	@DisplayName("A playable that has been paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			playable.play();
			playable.pause();
		}

		@DisplayName("is not playing")
		@Test
		void stopped() {
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be restarted")
		@Test
		void play() {
			playable.play();
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused again")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.pause());
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.pause());
		}
	}
}
