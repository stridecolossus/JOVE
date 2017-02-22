package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class PlayerGroupTest {
	private PlayerGroup group;
	private Player player;

	@Before
	public void before() {
		player = new Player();
		group = new PlayerGroup();
	}

	@Test
	public void constructor() {
		assertEquals(false, group.isPlaying());
		assertNotNull(group.getPlayers());
		assertEquals(true, group.getPlayers().isEmpty());
	}

	@Test
	public void play() {
		group.add(player);
		group.play();
		assertEquals(true, group.isPlaying());
		assertEquals(true, player.isPlaying());
	}

	@Test
	public void add() {
		group.add(player);
		assertEquals(true, group.getPlayers().contains(player));
	}

	@Test
	public void remove() {
		group.add(player);
		group.remove(player);
		assertEquals(true, group.getPlayers().isEmpty());
	}

	@Test
	public void clear() {
		group.add(player);
		group.clear();
		assertEquals(true, group.getPlayers().isEmpty());
	}
}
