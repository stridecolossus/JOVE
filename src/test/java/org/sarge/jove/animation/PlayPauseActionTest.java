package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PlayPauseActionTest {
	private PlayPauseAction action;
	private Player player;

	@Before
	public void before() {
		player = new Player();
		action = new PlayPauseAction(player);
	}

	@Test
	public void execute() {
		action.execute(null);
		assertEquals(true, player.isPlaying());
	}
}
