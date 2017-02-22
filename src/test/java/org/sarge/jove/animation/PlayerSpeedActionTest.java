package org.sarge.jove.animation;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class PlayerSpeedActionTest {
	private Player player;
	private PlayerSpeedAction action;
	
	@Before
	public void before() {
		action = null;
		player = new Player();
	}

	@Test
	public void executeScale() {
		action = new PlayerSpeedAction(player, MathsUtil.HALF, true);
		action.execute(null);
		assertFloatEquals(MathsUtil.HALF, player.getSpeed());
	}

	@Test
	public void executeAbsolute() {
		action = new PlayerSpeedAction(player, MathsUtil.HALF, false);
		action.execute(null);
		assertFloatEquals(MathsUtil.HALF, player.getSpeed());
	}
}
