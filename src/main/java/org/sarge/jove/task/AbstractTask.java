package org.sarge.jove.task;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.sarge.lib.util.ToString;

/**
 * Partial implementation.
 * @author Sarge
 */
public abstract class AbstractTask implements Task {
	protected static final Logger LOG = Logger.getLogger( Task.class.getName() );

	private Set<Listener> listeners;
	private State state = State.PENDING;

	@Override
	public State getState() {
		return state;
	}

	/**
	 * Updates the state of this task and notifies any listeners.
	 * @param state New state
	 */
	protected void setState( State state ) {
		// State change
		if( !this.state.isValid( state ) ) throw new IllegalArgumentException( "Invalid state change: from=" + this.state + " to=" + state );
		this.state = state;

		// Notify listeners
		if( listeners != null ) {
			for( Listener listener : listeners ) {
				listener.notify( this, state );
			}
		}
	}

	/**
	 * Adds a listener for state-change events on this task.
	 * @param listener Listener to add
	 */
	public void add( Listener listener ) {
		if( listeners == null ) {
			listeners = new HashSet<>(); // TODO - strict set
		}

		listeners.add( listener );
	}

	/**
	 * Removes the given listener from this task.
	 * @param listener Listener to remove
	 * @throws NullPointerException if the listener is null or has not been added
	 */
	public void remove( Listener listener ) {
		listeners.remove( listener );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
