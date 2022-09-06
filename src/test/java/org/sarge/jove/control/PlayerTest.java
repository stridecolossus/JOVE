package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Player.Listener;

class PlayerTest {
	private Player player;

	@BeforeEach
	void before() {
		player = new Player();
	}

	@DisplayName("A new player is initially stopped")
	@Test
	void constructor() {
		assertEquals(false, player.isPlaying());
	}

	@DisplayName("A player...")
	@Nested
	class PlayerTests {
		@DisplayName("can be played")
		@Test
		void play() {
			player.play();
			assertEquals(true, player.isPlaying());
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			player.play();
			player.pause();
			assertEquals(false, player.isPlaying());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			player.play();
			player.stop();
			assertEquals(false, player.isPlaying());
		}
	}

	@DisplayName("A playable added to a player...")
	@Nested
	class PlayableTests {
		private Playable playable;

		@BeforeEach
		void before() {
			playable = mock(Playable.class);
			player.add(playable);
		}

		@DisplayName("can be played")
		@Test
		void play() {
			player.play();
			verify(playable).play();
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			player.play();
			player.pause();
			verify(playable).pause();
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			player.play();
			player.stop();
			verify(playable).stop();
		}

		@DisplayName("can be removed from the player")
		@Test
		void remove() {
			player.remove(playable);
			player.play();
			verifyNoInteractions(playable);
		}
	}

	@DisplayName("A play listener...")
	@Nested
	class ListenerTests {
		private Listener listener;

		@BeforeEach
		void before() {
			listener = mock(Listener.class);
			player.add(listener);
		}

		@DisplayName("is notified when the player is played")
		@Test
		void play() {
			player.play();
			verify(listener).update(player);
		}

		@DisplayName("is notified when the player is paused")
		@Test
		void pause() {
			player.play();
			player.pause();
			verify(listener, times(2)).update(player);
		}

		@DisplayName("is notified when the player is stopped")
		@Test
		void stop() {
			player.play();
			player.stop();
			verify(listener, times(2)).update(player);
		}

		@DisplayName("can be removed from the player")
		@Test
		void remove() {
			player.remove(listener);
			player.play();
			verifyNoInteractions(listener);
		}
	}
}
