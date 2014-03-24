package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.Player.State;

public class PlayerGroupTest {
	private PlayerGroup group;
	private Player player;

	@Before
	public void before() {
		player = mock( Player.class );
		group = new PlayerGroup();
	}

	@Test
	public void constructor() {
		assertEquals( false, group.isPlaying() );
		assertNotNull( group.getPlayers() );
		assertEquals( true, group.getPlayers().isEmpty() );
	}

	@Test
	public void delegates() {
		group.add( player );
		group.setState( State.PLAYING );
		assertEquals( State.PLAYING, group.getState() );
		verify( player ).setState( State.PLAYING );
	}

	@Test
	public void add() {
		group.add( player );
		assertEquals( true, group.getPlayers().contains( player ) );
	}

	@Test
	public void remove() {
		group.add( player );
		group.remove( player );
		assertEquals( true, group.getPlayers().isEmpty() );
	}

	@Test
	public void clear() {
		group.add( player );
		group.clear();
		assertEquals( true, group.getPlayers().isEmpty() );
	}
}
