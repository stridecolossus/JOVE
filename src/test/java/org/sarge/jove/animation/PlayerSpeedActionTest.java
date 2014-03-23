package org.sarge.jove.animation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class PlayerSpeedActionTest {
	private PlayerSpeedAction action;
	private AbstractPlayer player;

	@Before
	public void before() {
		player = mock( AbstractPlayer.class );
		when( player.getSpeed() ).thenReturn( 0.5f );
		action = new PlayerSpeedAction( player, 0.5f, true );
	}

	@Test
	public void executeScale() {
		action.execute( null );
		verify( player ).setSpeed( 0.25f );
	}

	@Test
	public void executeAbsolute() {
		action = new PlayerSpeedAction( player, 0.5f, false );
		action.execute( null );
		verify( player ).setSpeed( 0.5f );
	}
}
