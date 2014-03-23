package org.sarge.jove.animation;

import org.sarge.jove.animation.Player.State;

/**
 * Listener for {@link Player} state change events.
 * @author Sarge
 */
public interface PlayerListener {
	/**
	 * Notifies a player state-change.
	 * @param player		Player
	 * @param state			New state
	 */
	void stateChanged( Player player, State state );
}
