package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Player.Listener;
import org.sarge.jove.control.Player.Playable;
import org.sarge.jove.control.Player.State;

public class PlayerTest {
	private Player player;
	private Playable playable;

	@BeforeEach
	public void before() {
		playable = mock(Playable.class);
		player = new Player(playable);
		when(playable.isPlaying()).thenReturn(true);
	}

	@Test
	public void constructor() {
		assertEquals(State.STOP, player.state());
		assertEquals(false, player.isPlaying());
		assertEquals(false, player.isRepeating());
	}

	@Test
	public void setRepeating() {
		player.setRepeating(true);
		assertEquals(true, player.isRepeating());
	}

	@Test
	public void play() {
		player.set(State.PLAY);
		assertEquals(State.PLAY, player.state());
		assertEquals(true, player.isPlaying());
	}

	@Test
	public void playAlreadyPlaying() {
		player.set(State.PLAY);
		assertThrows(IllegalStateException.class, () -> player.set(State.PLAY));
	}

	@Test
	public void stop() {
		player.set(State.PLAY);
		player.set(State.STOP);
		assertEquals(State.STOP, player.state());
		assertEquals(false, player.isPlaying());
	}

	@Test
	public void stopNotPlaying() {
		assertThrows(IllegalStateException.class, () -> player.set(State.STOP));
	}

	@Test
	public void pause() {
		player.set(State.PLAY);
		player.set(State.PAUSE);
		assertEquals(State.PAUSE, player.state());
		assertEquals(false, player.isPlaying());
	}

	@Test
	public void unpause() {
		player.set(State.PLAY);
		player.set(State.PAUSE);
		player.set(State.PLAY);
		assertEquals(State.PLAY, player.state());
		assertEquals(true, player.isPlaying());
	}

	@Test
	public void pauseNotPlaying() {
		assertThrows(IllegalStateException.class, () -> player.set(State.PAUSE));
	}

	@Test
	public void playableHasStopped() {
		player.set(State.PLAY);
		when(playable.isPlaying()).thenReturn(false);
		assertEquals(false, player.isPlaying());
	}

	@Test
	public void listener() {
		final Listener listener = mock(Listener.class);
		player.add(listener);
		player.set(State.PLAY);
		player.set(State.PAUSE);
		player.set(State.STOP);
		verify(listener).update(State.PLAY);
		verify(listener).update(State.PAUSE);
		verify(listener).update(State.STOP);
	}
}
