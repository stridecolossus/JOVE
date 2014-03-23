package org.sarge.jove.task;

import java.util.logging.Level;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Wrapper for an executable task.
 * @author Sarge
 */
public class DefaultTask extends AbstractTask implements Comparable<DefaultTask>, Runnable {
	private final RunnableTask task;
	private final int priority;

	/**
	 * Constructor.
	 * @param task			Task
	 * @param priority		Priority
	 */
	public DefaultTask( RunnableTask task, int priority ) {
		Check.notNull( task );
		this.task = task;
		this.priority = priority;
	}

	/**
	 * @return Underlying runnable task
	 */
	public RunnableTask getTask() {
		return task;
	}

	/**
	 * @return Priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Queues this task for execution.
	 */
	@Override
	public void start() {
		setState( State.QUEUED );
		final TaskQueue q = task.getQueue();
		q.add( this );
	}

	@Override
	public int compareTo( DefaultTask entry ) {
		return entry.priority - this.priority;
	}

	/**
	 * Cancels this task and removes it from the associated queue.
	 * @see TaskQueue#cancel(DefaultTask)
	 */
	@Override
	public void cancel() {
		setState( State.CANCELLED );
		final TaskQueue q = task.getQueue();
		q.cancel( this );
	}

	@Override
	public void run() {
		setState( State.RUNNING );
		try {
			task.run();
			setState( State.FINISHED );
		}
		catch( Exception e ) {
			setState( State.FAILED );
			LOG.log( Level.SEVERE, "Task failed: " + task, e );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
