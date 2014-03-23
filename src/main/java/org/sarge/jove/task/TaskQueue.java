package org.sarge.jove.task;

/**
 * Task execution queue.
 * @author Sarge
 */
public interface TaskQueue {
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
	 * @param task Task
	 */
	void add( DefaultTask task );

	/**
	 * Removes the task from the queue and terminates execution if running.
	 * @param task Task to cancel
	 */
	void cancel( DefaultTask task );
}
