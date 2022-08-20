package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.control.Playable.State.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Player.Listener;

class PlayerTest {
	private Player player;

	@BeforeEach
	void before() {
		player = new Player();
	}

	@Test
	void constructor() {
		assertEquals(STOP, player.state());
	}

	@DisplayName("A new player...")
	@Nested
	class New {
		@DisplayName("is initially stopped")
		@Test
		void isPlaying() {
			assertEquals(STOP, player.state());
		}

		@DisplayName("can be played")
		@Test
		void play() {
			player.state(PLAY);
			assertEquals(PLAY, player.state());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> player.state(PAUSE));
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> player.state(STOP));
		}
	}

	@DisplayName("A player that is currently playing...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			player.state(PLAY);
		}

		@DisplayName("cannot be played")
		@Test
		void play() {
			assertThrows(IllegalStateException.class, () -> player.state(PLAY));
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			player.state(PAUSE);
			assertEquals(PAUSE, player.state());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			player.state(STOP);
			assertEquals(STOP, player.state());
		}
	}

	@DisplayName("A playable that is paused...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			player.state(PLAY);
			player.state(PAUSE);
		}

		@DisplayName("can be unpaused")
		@Test
		void play() {
			player.state(PLAY);
			assertEquals(PLAY, player.state());
		}

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> player.state(PAUSE));
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			player.state(STOP);
			assertEquals(STOP, player.state());
		}
	}

	@DisplayName("A playable object controlled by a player...")
	@Nested
	class PlayableTests {
		private Playable playable;

		@BeforeEach
		void before() {
			playable = spy(Playable.class);
			player.add(playable);
		}

		@DisplayName("has state changes delegated to it")
		@Test
		void add() {
			player.state(PLAY);
			assertEquals(PLAY, playable.state());
		}

		@DisplayName("can be removed from the player")
		@Test
		void remove() {
			player.remove(playable);
			player.state(PLAY);
			verifyNoMoreInteractions(playable);
		}
	}

	@DisplayName("A listener attached to a player...")
	@Nested
	class ListenerTests {
		private Listener listener;

		@BeforeEach
		void before() {
			listener = mock(Listener.class);
		}

		@DisplayName("can be registered to a player")
		@Test
		void add() {
			player.add(listener);
			player.state(PLAY);
			verify(listener).update(player);
		}

		@DisplayName("can be removed from a player")
		@Test
		void remove() {
			player.add(listener);
			player.remove(listener);
			player.state(PLAY);
			verifyNoMoreInteractions(listener);
		}
	}
}
