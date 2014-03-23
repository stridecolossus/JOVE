package org.sarge.jove.task;

/**
 * Task definition.
 * @author Sarge
 */
public interface RunnableTask extends Runnable {
	/**
	 * @return Name of the queue for processing this task
	 */
	TaskQueue getQueue();
}
