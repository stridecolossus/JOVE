package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.control.Playable.State.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.Media;

public class PlayableTest {
	private Playable playable;

	@BeforeEach
	void before() {
		playable = spy(Playable.class);
	}

	@DisplayName("A new playable...")
	@Nested
	class New {
		@DisplayName("is initially stopped")
		@Test
		void isPlaying() {
			assertEquals(false, playable.isPlaying());
			assertEquals(STOP, playable.state());
		}

		@DisplayName("can be played")
		@Test
		void play() {
			playable.state(PLAY);
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.state(PAUSE));
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> playable.state(STOP));
		}
	}

	@DisplayName("A playable that is currently playing...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			playable.state(PLAY);
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be played")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> playable.state(PLAY));
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			playable.state(PAUSE);
			assertEquals(false, playable.isPlaying());
			assertEquals(PAUSE, playable.state());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.state(STOP);
			assertEquals(false, playable.isPlaying());
			assertEquals(STOP, playable.state());
		}
	}

	@DisplayName("A playable that is paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			playable.state(PLAY);
			playable.state(PAUSE);
			assertEquals(false, playable.isPlaying());
		}

		@DisplayName("can be unpaused")
		@Test
		void play() {
			playable.state(PLAY);
			assertEquals(true, playable.isPlaying());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> playable.state(PAUSE));
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			playable.state(STOP);
			assertEquals(false, playable.isPlaying());
			assertEquals(STOP, playable.state());
		}
	}

	@DisplayName("A playable is not repeating by default")
	@Test
	void isRepeating() {
		assertEquals(false, playable.isRepeating());
	}

	@DisplayName("A playable can be set to repeat")
	@Test
	void repeat() {
		playable.repeat(true);
		assertEquals(true, playable.isRepeating());
	}

	@DisplayName("A playable media...")
	@Nested
	class MediaTests {
		private Media media;

		@BeforeEach
		void before() {
			media = spy(Media.class);
		}

		@DisplayName("may stop playing in the background")
		@Test
		void stopped() {
			media.state(PLAY);
			when(media.isPlaying()).thenReturn(false);
			assertEquals(STOP, media.state());
		}

		@DisplayName("that has stopped in the background can be restarted")
		@Test
		void state() {
			when(media.isPlaying()).thenReturn(false);
			media.state(PLAY);
			when(media.isPlaying()).thenReturn(true);
			assertEquals(PLAY, media.state());
		}
	}
}
