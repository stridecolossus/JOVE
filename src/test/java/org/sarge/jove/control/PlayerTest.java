package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Player.Listener;

class PlayerTest {
	private Player player;
	private Playable playable;
	private Listener listener;

	@BeforeEach
	void before() {
		playable = spy(AbstractPlayable.class);
		listener = mock(Listener.class);
		player = new Player(playable);
		player.add(listener);
	}

	@Test
	void constructor() {
		assertEquals(false, player.isPlaying());
	}

	@Test
	void play() {
		player.play();
		assertEquals(true, player.isPlaying());
		verify(playable).play();
		verify(listener).update(player);
	}

	@Test
	void pause() {
		player.play();
		player.pause();
		assertEquals(false, player.isPlaying());
		verify(playable).pause();
		verify(listener, times(2)).update(player);
	}

	@Test
	void stop() {
		player.play();
		player.stop();
		assertEquals(false, player.isPlaying());
		verify(playable).stop();
		verify(listener, times(2)).update(player);
	}
}
