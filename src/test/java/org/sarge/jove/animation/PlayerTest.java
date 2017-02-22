package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.Player.Listener;
import org.sarge.jove.animation.Player.State;

public class PlayerTest {
	private Player player;

	@Before
	public void before() {
		player = new Player();
	}

	@Test
	public void constructor() {
		assertEquals(State.STOPPED, player.getState());
		assertEquals(false, player.isPlaying());
	}

	@Test
	public void setState() {
		// Start playing
		player.play();
		assertEquals(State.PLAYING, player.getState());
		assertEquals(true, player.isPlaying());

		// Pause
		player.pause();
		assertEquals(State.PAUSED, player.getState());
		assertEquals(false, player.isPlaying());

		// Continue playing
		player.play();
		assertEquals(State.PLAYING, player.getState());
		assertEquals(true, player.isPlaying());

		// Stop
		player.stop();
		assertEquals(State.STOPPED, player.getState());
		assertEquals(false, player.isPlaying());
	}

	@Test(expected = IllegalArgumentException.class)
	public void alreadyPlaying() {
		player.play();
		player.play();
	}

	@Test(expected = IllegalArgumentException.class)
	public void stopNotPlaying() {
		player.stop();
	}

	@Test(expected = IllegalArgumentException.class)
	public void pauseNotPlaying() {
		player.play();
		player.pause();
		player.pause();
	}

	@Test
	public void listeners() {
		final Listener listener = mock(Listener.class);
		player.add(listener);
		player.play();
		verify(listener).stateChanged(player, State.PLAYING);
	}
}
