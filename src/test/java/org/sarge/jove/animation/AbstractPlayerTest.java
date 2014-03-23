package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.AbstractPlayer;
import org.sarge.jove.animation.PlayerListener;
import org.sarge.jove.animation.Player.State;

public class AbstractPlayerTest {
	private AbstractPlayer player;

	@Before
	public void before() {
		player = new AbstractPlayer() {
			// Mock implementation
		};
	}

	@Test
	public void constructor() {
		assertEquals( State.STOPPED, player.getState() );
		assertEquals( false, player.isPlaying() );
	}

	@Test
	public void setState() {
		// Start playing
		player.setState( State.PLAYING );
		assertEquals( State.PLAYING, player.getState() );
		assertEquals( true, player.isPlaying() );

		// Pause
		player.setState( State.PAUSED );
		assertEquals( State.PAUSED, player.getState() );
		assertEquals( false, player.isPlaying() );

		// Continue playing
		player.setState( State.PLAYING );
		assertEquals( State.PLAYING, player.getState() );
		assertEquals( true, player.isPlaying() );

		// Stop
		player.setState( State.STOPPED );
		assertEquals( State.STOPPED, player.getState() );
		assertEquals( false, player.isPlaying() );
	}

	@Test( expected = IllegalArgumentException.class )
	public void alreadyPlaying() {
		player.setState( State.PLAYING );
		player.setState( State.PLAYING );
	}

	@Test( expected = IllegalArgumentException.class )
	public void stopNotPlaying() {
		player.setState( State.STOPPED );
	}

	@Test( expected = IllegalArgumentException.class )
	public void pauseNotPlaying() {
		player.setState( State.PLAYING );
		player.setState( State.PAUSED );
		player.setState( State.PAUSED );
	}

	@Test
	public void listeners() {
		final PlayerListener listener = mock( PlayerListener.class );
		player.add( listener );
		player.setState( State.PLAYING );
		verify( listener ).stateChanged( player, State.PLAYING );
	}
}
