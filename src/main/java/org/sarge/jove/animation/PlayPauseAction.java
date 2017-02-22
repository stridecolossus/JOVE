package org.sarge.jove.animation;

import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Play-pause toggle action for a {@link Player}.
 * @author Sarge
 */
public final class PlayPauseAction implements Action {
	private final Player player;

	/**
	 * Constructor.
	 * @param player Player
	 */
	public PlayPauseAction(Player player) {
		Check.notNull(player);
		this.player = player;
	}

	@Override
	public void execute(InputEvent event) {
		if(player.getState() == Player.State.PLAYING) {
			player.pause();
		}
		else {
			player.play();
		}
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
