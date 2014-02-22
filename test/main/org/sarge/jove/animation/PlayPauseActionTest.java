package org.sarge.jove.animation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class PlayPauseActionTest {
	private PlayPauseAction action;
	private Player player;

	@Before
	public void before() {
		player = mock( AbstractPlayer.class );
		action = new PlayPauseAction( player );
	}

	@Test
	public void execute() {
		action.execute( null );
		verify( player ).setState( Player.State.PLAYING );
	}
}
