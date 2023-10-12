package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;

class PlayerTest {
	private Player player;
	private Playable playable;
	private Consumer<Player> listener;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		playable = spy(AbstractPlayable.class);
		listener = mock(Consumer.class);
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
		verify(listener).accept(player);
	}

	@Test
	void pause() {
		player.play();
		player.pause();
		assertEquals(false, player.isPlaying());
		verify(playable).pause();
		verify(listener, times(2)).accept(player);
	}

	@Test
	void stop() {
		player.play();
		player.stop();
		assertEquals(false, player.isPlaying());
		verify(playable).stop();
		verify(listener, times(2)).accept(player);
	}
}
