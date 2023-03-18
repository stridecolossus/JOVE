package org.sarge.jove.scene.core;

import static org.sarge.lib.util.Check.oneOrMore;

import java.util.*;
import java.util.concurrent.*;

import org.sarge.jove.control.FrameTimer;
import org.sarge.jove.control.FrameTimer.Listener;
import org.sarge.lib.util.Check;

/**
 * The <i>render loop</i> performs frame rendering according to a configured frame rate.
 * <p>
 * Note that frame rendering tasks are executed sequentially on a single thread.
 * <p>
 * @see FrameTimer.Listener
 * @author Sarge
 */
public class RenderLoop {
	private final Set<Listener> listeners = new HashSet<>();
	private int rate;
	private Future<?> future;

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
		this.rate = oneOrMore(rate);
	}

	/**
	 * Registers a frame completion listener.
	 * @param listener Listener to add
	 */
	public void add(Listener listener) {
		Check.notNull(listener);
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
	 * Starts the render loop.
	 * @param task Render task
	 * @throws IllegalStateException if rendering has already been started
	 */
	public void start(Runnable task) {
		Check.notNull(task);
		if(isRunning()) {
			throw new IllegalStateException("Render loop has already been started");
		}

		final Runnable wrapper = () -> run(task);
		final long period = TimeUnit.SECONDS.toMillis(1) / rate;
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		future = executor.scheduleAtFixedRate(wrapper, 0, period, TimeUnit.MILLISECONDS);
	}

	/**
	 * Runs the given render task and tracks the elapsed duration.
	 * @param task Render task
	 * @return Frame
	 */
	private FrameTimer run(Runnable task) {
		final FrameTimer timer = new FrameTimer();
		task.run();
		timer.stop();
		update(timer);
		return timer;
	}

	/**
	 * Notifies listeners of a completed frame.
	 * @param frame Completed frame
	 */
	private void update(FrameTimer frame) {
		for(FrameTimer.Listener listener : listeners) {
			listener.update(frame);
		}
	}

	/**
	 * Stops the render loop.
	 * @throws IllegalStateException if rendering has not been started
	 */
	public void stop() {
		if(!isRunning()) {
			throw new IllegalStateException("Render loop has not been started");
		}
		future.cancel(true);
		future = null;
	}
}
