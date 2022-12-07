package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.State;
import org.sarge.jove.control.Player.Listener;

class PlayerTest {
	private Player player;
	private Playable playable;

	@BeforeEach
	void before() {
		playable = mock(Playable.class);
		player = new Player(playable);
	}

	@DisplayName("A new player is initially stopped")
	@Test
	void constructor() {
		assertEquals(false, player.isPlaying());
		assertEquals(playable, player.playable());
	}

	@DisplayName("A player...")
	@Nested
	class PlayerTests {
		@DisplayName("can be played")
		@Test
		void play() {
			player.apply(State.PLAY);
			when(playable.isPlaying()).thenReturn(true);
			assertEquals(true, player.isPlaying());
			verify(playable).apply(State.PLAY);
		}

		@DisplayName("can be paused")
		@Test
		void pause() {
			player.apply(State.PLAY);
			player.apply(State.PAUSE);
			assertEquals(false, player.isPlaying());
			verify(playable).apply(State.PAUSE);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			player.apply(State.PLAY);
			player.apply(State.STOP);
			assertEquals(false, player.isPlaying());
			verify(playable).apply(State.STOP);
		}

		@DisplayName("is stopped if the underlying playable is stopped")
		@Test
		void stopped() {
			player.apply(State.PLAY);
			when(playable.isPlaying()).thenReturn(false);
			assertEquals(false, player.isPlaying());
		}
	}

	@DisplayName("A player state change listener...")
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
			player.apply(State.PLAY);
			player.apply(State.PAUSE);
			player.apply(State.STOP);
			verify(listener, times(3)).update(player);
		}

		@DisplayName("can be removed from the player")
		@Test
		void remove() {
			player.remove(listener);
			player.apply(State.PLAY);
			verifyNoInteractions(listener);
		}
	}
}
