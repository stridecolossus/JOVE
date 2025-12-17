package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.control.Playable.State.*;

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
			playable.state(PLAYING);
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.state(PAUSED));
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.state(STOPPED));
		}
	}

	@DisplayName("A playable that has been started...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			playable.state(PLAYING);
		}

		@DisplayName("is playing")
		@Test
		void stopped() {
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be played again")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> playable.state(PLAYING));
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			playable.state(PAUSED);
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.state(STOPPED);
			assertEquals(false, playable.isPlaying());
		}
	}

	@DisplayName("A playable that has been paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			playable.state(PLAYING);
			playable.state(PAUSED);
		}

		@DisplayName("is not playing")
		@Test
		void stopped() {
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be restarted")
		@Test
		void play() {
			playable.state(PLAYING);
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused again")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.state(PAUSED));
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.state(STOPPED);
		}
	}
}
