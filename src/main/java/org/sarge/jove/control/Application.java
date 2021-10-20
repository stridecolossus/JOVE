package org.sarge.jove.control;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sarge.jove.platform.desktop.Desktop;

/**
 * An <i>application</i> is comprised of a number of <i>tasks</i> to be continually executed.
 * <p>
 * The {@link #run()} method is an infinite loop terminated by the {@link #stop()} method.
 * <p>
 * In general {@link #run()} is invoked executed on the main thread of the application, see {@link Desktop#poll()}.
 * <p>
 * @author Sarge
 */
public class Application {
	private final List<Runnable> steps;
	private final AtomicBoolean running = new AtomicBoolean();

	/**
	 * Constructor.
	 * @param steps Execution steps of this application
	 */
	public Application(List<Runnable> steps) {
		this.steps = List.copyOf(steps);
	}

	/**
	 * @return Whether this application is running
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * Runs this application until stopped.
	 */
	public void run() {
		running.set(true);
		while(isRunning()) {
			steps.forEach(Runnable::run);
		}
	}

	/**
	 * Stops this application.
	 * @throws IllegalStateException if the application is not running
	 */
	public void stop() {
		if(!isRunning()) throw new IllegalStateException("Not running");
		running.set(false);
	}
}
