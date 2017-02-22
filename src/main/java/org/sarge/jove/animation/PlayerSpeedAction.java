package org.sarge.jove.animation;

import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Player speed control.
 * @author Sarge
 */
public class PlayerSpeedAction implements Action {
	private final Player player;
	private final float speed;
	private final boolean scale;

	/**
	 * Constructor.
	 * @param player	Player
	 * @param speed		Speed
	 * @param scale		Whether to explicitly <b>set</b> the speed or <b>scale</b> the current player speed
	 */
	public PlayerSpeedAction(Player player, float speed, boolean scale) {
		Check.notNull(player);
		this.player = player;
		this.speed = speed;
		this.scale = scale;
	}

	@Override
	public void execute(InputEvent data) {
		if(scale) {
			player.setSpeed(player.getSpeed() * speed);
		}
		else {
			player.setSpeed(speed);
		}
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
