package org.sarge.jove.control;

import java.util.List;

import org.sarge.jove.platform.desktop.Desktop;

/**
 * The <i>render loop</i> comprised of a number of <i>tasks</i> to be executed as part of the rendering loop.
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

	private final List<Task> tasks;
	private volatile boolean running;

	/**
	 * Constructor.
	 * @param steps Execution steps of this application
	 */
	public RenderLoop(List<Task> steps) {
		this.tasks = List.copyOf(steps);
	}

	/**
	 * @return Whether this application is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Runs this application until stopped.
	 */
	public void run() {
		running = true;
		while(running) {
			tasks.forEach(Task::execute);
		}
	}

	/**
	 * Stops this application.
	 * @throws IllegalStateException if the application is not running
	 */
	public void stop() {
		if(!running) throw new IllegalStateException("Not running");
		running = false;
	}
}
