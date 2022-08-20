package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.control.Playable.State.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.control.Playable.*;

public class PlayableTest {
	private Playable playable;

	@BeforeEach
	void before() {
		playable = spy(Playable.class);
	}

	@Test
	void constructor() {
		assertEquals(STOP, playable.state());
		assertEquals(false, playable.isPlaying());
		assertEquals(false, playable.isRepeating());
	}

	@DisplayName("A playable can be played")
	@Test
	void state() {
		playable.state(PLAY);
		assertEquals(PLAY, playable.state());
		assertEquals(true, playable.isPlaying());
	}

	@DisplayName("A playable can be repeating")
	@Test
	void repeat() {
		playable.repeat(true);
		assertEquals(true, playable.isRepeating());
	}

	@DisplayName("A playable...")
	@Nested
	class StateTests {
		@DisplayName("can be played")
		@Test
		void play() {
			STOP.validate(PLAY);
		}

		@DisplayName("that is playing can be paused")
		@Test
		void pause() {
			PLAY.validate(PAUSE);
		}

		@DisplayName("that is paused can be played")
		@Test
		void unpause() {
			PAUSE.validate(PLAY);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			PLAY.validate(STOP);
			PAUSE.validate(STOP);
		}

		@DisplayName("cannot be transitioned to the same state")
		@ParameterizedTest
		@EnumSource(State.class)
		void duplicate(State state) {
			assertThrows(IllegalStateException.class, () -> state.validate(state));
		}

		@DisplayName("cannot be paused unless it is playing")
		@Test
		void invalid() {
			assertThrows(IllegalStateException.class, () -> STOP.validate(PAUSE));
		}
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
