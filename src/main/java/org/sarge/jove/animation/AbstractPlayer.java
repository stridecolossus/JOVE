package org.sarge.jove.animation;

import java.util.Set;

import org.sarge.lib.util.StrictSet;
import org.sarge.lib.util.ToString;

/**
 * Template implementation.
 * @author Sarge
 */
public abstract class AbstractPlayer implements Player {
	private final Set<PlayerListener> listeners = new StrictSet<>();

	private State state = State.STOPPED;
	private float speed = 1;
	private boolean repeating;

	@Override
	public State getState() {
		return state;
	}

	/**
	 * @return Whether this player is currently playing
	 */
	public boolean isPlaying() {
		return state == State.PLAYING;
	}

	@Override
	public void setState( State state ) {
		// Verify state
		switch( state ) {
		case PLAYING:
			if( this.state == State.PLAYING ) throw new IllegalArgumentException( "Already playing: " + this );
			break;

		case PAUSED:
			if( this.state != State.PLAYING ) throw new IllegalArgumentException( "Not playing: " + this );
			break;

		case STOPPED:
			if( this.state == State.STOPPED ) throw new IllegalArgumentException( "Already stopped: " + this );
			break;
		}

		// Update state
		this.state = state;

		// notify listeners
		for( PlayerListener p : listeners ) {
			p.stateChanged( this, state );
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
	public void setSpeed( float speed ) {
		if( speed <= 0 ) throw new IllegalArgumentException( "Speed must be positive" );
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
	public void setRepeating( boolean repeating ) {
		this.repeating = repeating;
	}

	/**
	 * Adds a listener.
	 * @param p Listener to add
	 */
	public void add( PlayerListener p ) {
		listeners.add( p );
	}

	/**
	 * Removes a listener.
	 * @param p Listener to remove
	 */
	public void remove( PlayerListener p ) {
		listeners.remove( p );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
