package org.sarge.jove.task;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictSet;
import org.sarge.lib.util.ToString;

/**
 * Default task which wraps a prioritised {@link Runnable} work-unit with an associated execution queue.
 * @author Sarge
 */
public class Task implements Runnable, Comparable<Task> {
	private static final Logger LOG = Logger.getLogger( Task.class.getName() );

	/**
	 * Task state.
	 */
	public static enum State {
		PENDING(),
		QUEUED( PENDING ),
		RUNNING( QUEUED ),
		FINISHED( RUNNING ),
		FAILED( RUNNING ),
		CANCELLED( PENDING, QUEUED, RUNNING );

		private final Set<State> predecessors;

		private State( State... prev ) {
			this.predecessors = new HashSet<>( Arrays.asList( prev ) );		// Note - cannot use EnumSet here sadly
		}

		/**
		 * @return Valid predecessor state(s) for this state
		 */
		public Set<State> getPredecessors() {
			return Collections.unmodifiableSet( predecessors );
		}

		/**
		 * @param next Next state
		 * @return Whether the given state is a valid state-change from this state
		 */
		public boolean isValid( State next ) {
			return next.predecessors.contains( this );
		}
	}

	/**
	 * Task state-change listener.
	 */
	public static interface Listener {
		/**
		 * Notifies a task state-change event.
		 * @param task		Changed task
		 * @param state		New state
		 */
		void notify( Task task, State state );
	}

	private final Runnable runnable;
	private final int priority;
	private final TaskQueue queue;

	private State state = State.PENDING;
	private Set<Listener> listeners;

	/**
	 * Constructor.
	 * @param runnable		Runnable work-unit
	 * @param priority		Priority
	 * @param queue			Execution queue
	 */
	public Task( Runnable runnable, int priority, TaskQueue queue ) {
		Check.notNull( runnable );
		Check.notNull( queue );
		if( runnable instanceof Task ) throw new IllegalArgumentException( "Runnable cannot be a task" );

		this.runnable = runnable;
		this.priority = priority;
		this.queue = queue;
	}

	/**
	 * @return Task priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return Execution queue
	 */
	public TaskQueue getQueue() {
		return queue;
	}

	/**
	 * @return Underlying work-unit
	 */
	public Runnable getRunnable() {
		return runnable;
	}

	/**
	 * @return Current state of this task
	 */
	public State getState() {
		return state;
	}

	/**
	 * Advances the state of this task and notifies any listeners.
	 * @param state State change
	 */
	void setState( State state ) {
		// Update state
		if( !this.state.isValid( state ) ) throw new RuntimeException( "Invalid state change: from=" + this.state + " to=" + state + " task=" + this );
		this.state = state;

		// Notify listeners
		if( listeners != null ) {
			try {
				for( Listener listener : listeners ) {
					listener.notify( this, state );
				}
			}
			catch( Exception e ) {
				LOG.log( Level.SEVERE, "Listener exception: task=" + this, e );
			}
		}
	}

	/**
	 * Attaches a listener to this task.
	 * @param listener Listener
	 */
	public void add( Listener listener ) {
		if( listeners == null ) {
			listeners = new StrictSet<>();
		}

		listeners.add( listener );
	}

	/**
	 * Starts this task.
	 */
	public void start() {
		queue.add( runnable );
		setState( State.QUEUED );
	}

	/**
	 * Cancels this task.
	 */
	public void cancel() {
		queue.cancel( runnable );
		setState( State.CANCELLED );
	}

	@Override
	public int compareTo( Task that ) {
		return that.priority - this.priority;
	}

	@Override
	public void run() {
		setState( State.RUNNING );
		try {
			runnable.run();
		}
		catch( Exception e ) {
			LOG.log( Level.SEVERE, "Task failed: " + Task.this, e );
			setState( State.FAILED );
			return;
		}
		setState( State.FINISHED );
	}

	@Override
	public String toString() {
		final ToString str = new ToString( this );
		str.append( "task", runnable );
		str.append( "priority", priority );
		str.append( "queue", queue.getName() );
		str.append( "state", state );
		return str.toString();
	}
}
