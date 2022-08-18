package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

	@DisplayName("A playable object...")
	@Nested
	class PlayableTests {
		private Playable playable;

		@BeforeEach
		void before() {
			playable = spy(Playable.class);
			player.add(playable);
		}

		@DisplayName("can be added to a player")
		@Test
		void add() {
			player.state(PLAY);
			assertEquals(PLAY, player.state());
			assertEquals(PLAY, playable.state());
		}

		@DisplayName("can be removed from a player")
		@Test
		void remove() {
			player.remove(playable);
			player.state(PLAY);
			verifyNoMoreInteractions(playable);
		}
	}

	@DisplayName("A player listener...")
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
