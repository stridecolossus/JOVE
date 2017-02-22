package org.sarge.jove.animation;

import java.util.Collections;
import java.util.Set;

import org.sarge.lib.util.StrictSet;

/**
 * Group of players.
 * @author Sarge
 */
public class PlayerGroup extends Player {
	private final Set<Player> players = new StrictSet<>();

	/**
	 * @return Players in this group
	 */
	public Set<Player> getPlayers() {
		return Collections.unmodifiableSet(players);
	}

	/**
	 * Adds a player to this group.
	 * @param p Player to add
	 */
	public void add(Player p) {
		players.add(p);
	}

	/**
	 * Removes a player from this group.
	 * @param p Player to remove
	 */
	public void remove(Player p) {
		players.remove(p);
	}

	/**
	 * Removes all players from this group.
	 */
	public void clear() {
		players.clear();
	}

	@Override
	public void play() {
		super.play();
		for(Player player : players) {
			player.play();
		}
	}

	@Override
	public void pause() {
		super.pause();
		for(Player player : players) {
			player.pause();
		}
	}

	@Override
	public void stop() {
		super.stop();
		for(Player player : players) {
			player.stop();
		}
	}

	@Override
	public void setSpeed(float speed) {
		super.setSpeed(speed);
		for(Player player : players) {
			player.setSpeed(speed);
		}
	}

	@Override
	public void setRepeating(boolean repeating) {
		super.setRepeating(repeating);
		for(Player player : players) {
			player.setRepeating(repeating);
		}
	}
}
