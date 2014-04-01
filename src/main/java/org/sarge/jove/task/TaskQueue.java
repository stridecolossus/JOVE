package org.sarge.jove.task;

/**
 * Task execution queue.
 * @author Sarge
 */
public interface TaskQueue {
	/**
	 * @return Queue identifier
	 */
	String getName();

	/**
	 * @return Current queue size
	 */
	int getSize();

	/**
	 * @return Maximum queue size
	 */
	int getMaximumSize();

	/**
	 * Adds a task to this queue.
	 * @param r Task to run
	 */
	void add( Runnable r );

	/**
	 * Removes the task from the queue and terminates execution if running.
	 * @param r Task to cancel
	 */
	void cancel( Runnable r );
}
