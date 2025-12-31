package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.State;

class PlayerTest {
	private Player player;
	private AbstractPlayable playable;
	private AtomicReference<Player> listener;

	@BeforeEach
	void before() {
		playable = new AbstractPlayable() {
			// Empty
		};

		listener = new AtomicReference<>();

		player = new Player(playable);
		player.add(listener::set);
	}

	@Test
	void constructor() {
		assertEquals(false, player.isPlaying());
		assertEquals(false, playable.isPlaying());
		assertEquals(null, listener.get());
	}

	@Test
	void play() {
		player.state(State.PLAYING);
		assertEquals(true, player.isPlaying());
		assertEquals(true, playable.isPlaying());
		assertEquals(player, listener.get());
	}

	@Test
	void pause() {
		player.state(State.PLAYING);
		player.state(State.PAUSED);
		assertEquals(State.PAUSED, playable.state());
		assertEquals(false, playable.isPlaying());
		assertEquals(false, player.isPlaying());
		assertEquals(player, listener.get());
	}

	@Test
	void stop() {
		player.state(State.PLAYING);
		player.state(State.STOPPED);
		assertEquals(false, player.isPlaying());
		assertEquals(false, playable.isPlaying());
		assertEquals(player, listener.get());
	}
}
