package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Player.Listener;
import org.sarge.jove.control.Player.State;

class PlayerTest {
	private Player player;

	@BeforeEach
	void before() {
		player = new Player();
	}

	@Test
	void constructor() {
		assertEquals(State.STOP, player.state());
		assertEquals(false, player.isRepeating());
	}

	@Test
	void play() {
		player.state(State.PLAY);
		assertEquals(State.PLAY, player.state());
	}

	@Test
	void pause() {
		player.state(State.PLAY);
		player.state(State.PAUSE);
		assertEquals(State.PAUSE, player.state());
	}

	@Test
	void unpause() {
		player.state(State.PLAY);
		player.state(State.PAUSE);
		player.state(State.PLAY);
		assertEquals(State.PLAY, player.state());
	}

	@Test
	void stop() {
		player.state(State.PLAY);
		player.state(State.STOP);
		assertEquals(State.STOP, player.state());
	}

	@Test
	void playAlreadyPlaying() {
		player.state(State.PLAY);
		assertThrows(IllegalStateException.class, () -> player.state(State.PLAY));
	}

	@Test
	void pauseNotPlaying() {
		assertThrows(IllegalStateException.class, () -> player.state(State.PAUSE));
	}

	@Test
	void stopAlreadyStopped() {
		assertThrows(IllegalStateException.class, () -> player.state(State.STOP));
	}

	@Test
	void repeat() {
		player.repeat(true);
		assertEquals(true, player.isRepeating());
	}

	@Test
	void listener() {
		final Listener listener = mock(Listener.class);
		player.add(listener);
		player.state(State.PLAY);
		player.state(State.PAUSE);
		player.state(State.STOP);
		verify(listener).update(State.PLAY);
		verify(listener).update(State.PAUSE);
		verify(listener).update(State.STOP);
	}

	@Test
	void remove() {
		final Listener listener = mock(Listener.class);
		player.add(listener);
		player.remove(listener);
		player.state(State.PLAY);
		verifyNoMoreInteractions(listener);
	}
}
