package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.platform.desktop.Desktop;

/**
 * The <i>render loop</i> is comprised of a number of <i>tasks</i> to be executed as part of the rendering loop.
 * <p>
 * The {@link #run()} method is an infinite loop terminated by the {@link #stop()} method.
 * <p>
 * In general {@link #run()} is invoked executed on the main thread of the application, see {@link Desktop#poll()}.
 * <p>
 * @author Sarge
 */
public class RenderLoop {
	/**
	 * A <i>task</i> is executed by the render loop.
	 */
	@FunctionalInterface
	public interface Task {
		/**
		 * Performs this task.
		 */
		void execute();
	}

	private final List<Task> tasks = new ArrayList<>();
	private volatile boolean running;

	/**
	 * Default constructor.
	 */
	public RenderLoop() {
	}

	/**
	 * Convenience constructor given a list of tasks.
	 * @param tasks Render loop tasks
	 * @see #add(Task)
	 */
	public RenderLoop(List<Task> tasks) {
		tasks.forEach(this::add);
	}

	/**
	 * Adds a task to the render loop.
	 * @param task Task to add
	 */
	public void add(Task task) {
		tasks.add(notNull(task));
	}

	/**
	 * Removes a task from the render loop.
	 * @param task Task to remove
	 */
	public void remove(Task task) {
		tasks.remove(task);
	}

	/**
	 * @return Whether this render loop is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Runs this loop until stopped.
	 */
	public void run() {
		running = true;
		while(running) {
			for(Task task : tasks) {
				task.execute();
			}
		}
	}

	/**
	 * Stops the render loop.
	 * @throws IllegalStateException if the loop is not running
	 */
	public void stop() {
		if(!running) throw new IllegalStateException("Not running");
		running = false;
	}
}
