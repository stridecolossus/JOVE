package org.sarge.jove.scene.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import org.sarge.jove.control.Frame;
import org.sarge.jove.control.Frame.Listener;

/**
 * The <i>render loop</i> performs frame rendering according to a configured frame rate.
 * <p>
 * Note that frame rendering tasks are executed sequentially on a single thread.
 * <p>
 * @see Frame.Listener
 * @author Sarge
 */
public class RenderLoop {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final Set<Listener> listeners = new HashSet<>();
	private int rate;
	private Runnable task;
	private Future<?> future;
	private Consumer<Exception> handler = Exception::printStackTrace;
	private boolean paused;

	/**
	 * Constructor.
	 */
	public RenderLoop() {
		rate(60);
	}

	/**
	 * @return Whether this render loop is running
	 */
	public boolean isRunning() {
		return future != null;
	}

	/**
	 * @return Target frame rate (or FPS)
	 */
	public int rate() {
		return rate;
	}

	/**
	 * Sets the target frame rate (or FPS).
	 * @param rate Frame rate
	 * @throws IllegalArgumentException if {@link #rate} is not one-or-more
	 * @throws IllegalStateException if this loop is running
	 */
	public void rate(int rate) {
		if(isRunning()) throw new IllegalStateException("Cannot set frame rate while running");
		this.rate = requireOneOrMore(rate);
	}

	/**
	 * Registers a frame completion listener.
	 * @param listener Listener to add
	 */
	public void add(Listener listener) {
		requireNonNull(listener);
		listeners.add(listener);
	}

	/**
	 * Detaches a frame completion listener.
	 * @param listener Listener to remove
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Sets the handler for exceptions in the render task (default dumps to the error console).
	 * @param handler Exception handler
	 */
	public void handler(Consumer<Exception> handler) {
		this.handler = requireNonNull(handler);
	}

	/**
	 * Starts the render loop.
	 * @param task Render task
	 * @throws IllegalStateException if rendering has already been started
	 */
	public void start(Runnable task) {
		if(isRunning()) throw new IllegalStateException("Loop is already running");
		this.task = requireNonNull(task);
		schedule();
	}

	/**
	 * Starts scheduling of the render task.
	 */
	private void schedule() {
		assert task != null;
		final long period = TimeUnit.SECONDS.toMillis(1) / rate;
		future = executor.scheduleAtFixedRate(this::run, 0, period, TimeUnit.MILLISECONDS);
	}

	/**
	 * Runs the given render task and tracks the elapsed duration.
	 * @param task Render task
	 */
	private void run() {
		final Frame timer = new Frame();
		try {
			task.run();
			// TODO - latch?
		}
		catch(Exception e) {
			handler.accept(e);
		}
		timer.stop();
		update(timer);
	}

	/**
	 * Notifies listeners of a completed frame.
	 * @param frame Completed frame
	 */
	private void update(Frame frame) {
		for(Frame.Listener listener : listeners) {
			listener.update(frame);
		}
	}

	/**
	 * Pauses the render loop.
	 * @throws IllegalStateException if the loop is not running or is already paused
	 */
	public void pause() {
		if(!isRunning()) throw new IllegalStateException("Loop has not been started");
		if(paused) throw new IllegalStateException("Loop is already paused");
		future.cancel(true);
		paused = true;
	}

	/**
	 * Restarts a paused render loop.
	 * @throws IllegalStateException if the loop is not paused
	 */
	public void restart() {
		if(!paused) throw new IllegalStateException("Loop is not paused");
		schedule();
		paused = false;
	}

	/**
	 * Stops the render loop.
	 * @throws IllegalStateException if rendering has not been started
	 */
	public void stop() {
		if(!isRunning()) throw new IllegalStateException("Loop has not been started");
		future.cancel(true);
		future = null;
		paused = false;
	}
}
