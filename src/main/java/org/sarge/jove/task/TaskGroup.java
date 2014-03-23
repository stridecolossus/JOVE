package org.sarge.jove.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Group of inter-dependent tasks.
 * @author Sarge
 */
public class TaskGroup extends AbstractTask {
	/**
	 * Listener for lifecycle notifications from this set of tasks.
	 */
	protected final Listener listener = new Listener() {
		@Override
		public void notify( Task task, State state ) {
			// Ignore self
			if( task == TaskGroup.this ) return;

			// Handle state change
			switch( state ) {
			case FINISHED:
				// Update completed task
				if( !pending.contains( task ) ) throw new RuntimeException( "Unexpected task: " + task );
				pending.remove( task );
				break;

			case FAILED:
			case CANCELLED:
				// Cancel any remaining tasks and stop
				setState( state );
				cancelPendingTasks();
				return;

			default:
				// Ignore other lifecycle notifications
				return;
			}

			// Wait until all pending tasks are complete
			if( !pending.isEmpty() ) return;

			if( itr.hasNext() ) {
				// Start next stage
				next();
			}
			else {
				// Task finished
				reset();
				setState( State.FINISHED );
			}
		}
	};

	private final List<DefaultTask[]> tasks;
	private final Set<DefaultTask> pending = new HashSet<>();

	private Iterator<DefaultTask[]> itr;

	/**
	 * Constructor.
	 * @param tasks Set of tasks
	 */
	public TaskGroup( List<DefaultTask[]> tasks ) {
		this.tasks = new ArrayList<>( tasks );
		add( listener );
	}

	@Override
	public void start() {
		setState( State.QUEUED );
		setState( State.RUNNING );
		if( itr != null ) throw new RuntimeException( "Task already started" );
		itr = tasks.iterator();
		next();
	}

	/**
	 * Starts the next set of tasks.
	 */
	private void next() {
		pending.clear();
		final DefaultTask[] next = itr.next();
		for( DefaultTask t : next ) {
			t.start();
			pending.add( t );
		}
	}

	/**
	 * Resets this task group.
	 */
	private void reset() {
		pending.clear();
		itr = null;
	}

	@Override
	public void cancel() {
		setState( State.CANCELLED );
		cancelPendingTasks();
	}

	private void cancelPendingTasks() {
		// Cancel all current sub-tasks
		for( DefaultTask t : pending ) {
			t.cancel();
			t.setState( State.CANCELLED );
		}

		// Cancel sub-tasks not yet started
		while( itr.hasNext() ) {
			for( DefaultTask t : itr.next() ) {
				t.setState( State.CANCELLED );
			}
		}

		// Reset this group
		reset();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
