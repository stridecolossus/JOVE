package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.State;

class PlayableTest {
	private Playable playable;

	@BeforeEach
	void before() {
		playable = new Playable();
	}

	@DisplayName("A new playable is initially stopped")
	@Test
	void constructor() {
		assertEquals(false, playable.isPlaying());
		assertEquals(State.STOP, playable.state());
	}

	@DisplayName("A playable that is stopped...")
	@Nested
	class Stopped {
		@DisplayName("can be played")
		@Test
		void play() {
			playable.apply(State.PLAY);
			assertEquals(true, playable.isPlaying());
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
	}

	@DisplayName("A playable that is playing...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			playable.apply(State.PLAY);
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
			assertEquals(State.PAUSE, playable.state());
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.apply(State.STOP);
			assertEquals(State.STOP, playable.state());
			assertEquals(false, playable.isPlaying());
		}
	}

	@DisplayName("A playable that is paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			playable.apply(State.PLAY);
			playable.apply(State.PAUSE);
		}

		@DisplayName("can be restarted")
		@Test
		void play() {
			playable.apply(State.PLAY);
			assertEquals(true, playable.isPlaying());
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
}
