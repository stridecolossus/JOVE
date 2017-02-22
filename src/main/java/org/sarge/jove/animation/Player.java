package org.sarge.jove.animation;

import java.util.Set;

import org.sarge.lib.util.StrictSet;
import org.sarge.lib.util.ToString;

/**
 * Controller for something that can be played.
 * @author Sarge
 */
public class Player {
	/**
	 * Player state.
	 */
	public static enum State {
		STOPPED,
		PLAYING,
		PAUSED
	}
	
	/**
	 * Listener for state change events.
	 */
	public interface Listener {
		/**
		 * Notifies a player state-change.
		 * @param player		Player
		 * @param state			New state
		 */
		void stateChanged(Player player, State state);
	}

	private final Set<Listener> listeners = new StrictSet<>();

	private State state = State.STOPPED;
	private float speed = 1;
	private boolean repeating;

	/**
	 * @return State of this player
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return Whether this player is currently playing
	 */
	public boolean isPlaying() {
		return state == State.PLAYING;
	}
	
	/**
	 * Starts this player.
	 * @throws IllegalArgumentException if already playing
	 */
	public void play() {
		if(this.state == State.PLAYING) throw new IllegalArgumentException("Already playing: " + this);
		update(State.PLAYING);
	}
	
	/**
	 * Pauses this player.
	 * @throws IllegalArgumentException if not playing
	 */
	public void pause() {
		if(this.state != State.PLAYING) throw new IllegalArgumentException("Not playing: " + this);
		update(State.PAUSED);
	}
	
	/**
	 * Stops this player.
	 * @throws IllegalArgumentException if already stopped
	 */
	public void stop() {
		if(this.state == State.STOPPED) throw new IllegalArgumentException("Already stopped: " + this);
		update(State.STOPPED);
	}
	
	/**
	 * Updates the state of this player and notifies any listeners.
	 * @param state New state
	 */
	private void update(State state) {
		// Update state
		this.state = state;

		// notify listeners
		for(Listener p : listeners) {
			p.stateChanged(this, state);
		}
	}

	/**
	 * @return Player speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed of this player.
	 * @param speed Player speed
	 */
	public void setSpeed(float speed) {
		if(speed <= 0) throw new IllegalArgumentException("Speed must be positive");
		this.speed = speed;
	}

	/**
	 * @return Whether this is a repeating player
	 */
	public boolean isRepeating() {
		return repeating;
	}

	/**
	 * Sets whether this player is repeating.
	 * @param repeating
	 */
	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
	}

	/**
	 * Adds a listener.
	 * @param p Listener to add
	 */
	public void add(Listener p) {
		listeners.add(p);
	}

	/**
	 * Removes a listener.
	 * @param p Listener to remove
	 */
	public void remove(Listener p) {
		listeners.remove(p);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
