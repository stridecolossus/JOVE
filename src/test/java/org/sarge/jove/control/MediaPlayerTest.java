package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Player.State;

class MediaPlayerTest {
	private MediaPlayer player;
	private Playable playable;

	@BeforeEach
	void before() {
		playable = mock(Playable.class);
		player = new MediaPlayer(playable);
		when(playable.isPlaying()).thenReturn(true);
	}

	@Test
	void constructor() {
		assertEquals(State.STOP, player.state());
		assertEquals(false, player.isRepeating());
	}

	@Test
	void play() {
		player.state(State.PLAY);
		verify(playable).state(State.PLAY);
	}

	@Test
	void states() {
		player.state(State.PLAY);
		player.state(State.PAUSE);
		player.state(State.PLAY);
		player.state(State.STOP);
	}

	@Test
	void repeat() {
		player.repeat(true);
		verify(playable).repeat(true);
	}

	@Test
	void stopped() {
		player.state(State.PLAY);
		when(playable.isPlaying()).thenReturn(false);
		assertEquals(State.STOP, player.state());
	}
}
