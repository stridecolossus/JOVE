package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.concurrent.*;

import org.sarge.lib.util.Check;

/**
 * The <i>render loop</i> schedules render tasks and notifies interested listeners on frame completion.
 * @author Sarge
 */
public class RenderLoop {
	/**
	 * Listener for frame completion.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies a completed frame.
		 * @param elapsed Elapsed time (ms)
		 */
		void frame(long elapsed);
	}

	private final Set<Listener> listeners = new HashSet<>();
	private final ScheduledExecutorService executor;
	private ScheduledFuture<?> future;
	private long rate;

	/**
	 * Constructor.
	 * @param executor Task executor
	 */
	public RenderLoop(ScheduledExecutorService executor) {
		this.executor = notNull(executor);
		rate(60);
	}

	/**
	 * Default constructor using a new executor.
	 */
	public RenderLoop() {
		this(Executors.newSingleThreadScheduledExecutor());
	}

	/**
	 * @return Whether this render loop is running
	 */
	public boolean isRunning() {
		return future != null;
	}

	/**
	 * @return Frame rate, i.e. expected duration per frame
	 */
	public long rate() {
		return rate;
	}

	/**
	 * Sets the target frame-rate (default is 60 FPS).
	 * @param fps Frame-per-second
	 */
	public void rate(int fps) {
		check();
		this.rate = TimeUnit.SECONDS.toMillis(1) / fps;
	}

	/**
	 * Registers a frame listener.
	 * @param listener Listener to add
	 */
	public void add(Listener listener) {
		listeners.add(notNull(listener));
	}

	/**
	 * @throws IllegalStateException if this loop is running
	 */
	private void check() {
		if(future != null) throw new IllegalStateException("Render loop has already been started");
	}

	/**
	 * Starts the render loop.
	 * @param render Render task
	 * @throws IllegalStateException if rendering has already been started
	 */
	public void start(Runnable render) {
		Check.notNull(render);
		check();

		// Create listener adapter
		final Runnable wrapper = () -> {
			final long start = System.currentTimeMillis();
			render.run();

			final long elapsed = System.currentTimeMillis() - start;
			for(Listener listener : listeners) {
				listener.frame(elapsed);
			}
		};

		// Start render loop
		future = executor.scheduleAtFixedRate(wrapper, 0, rate, TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the render loop.
	 * @throws IllegalStateException if rendering has not been started
	 */
	public void stop() {
		if(future == null) throw new IllegalStateException("Render loop has not been started");
		future.cancel(true);
		future = null;
	}

	public void close() {
		executor.shutdownNow();
	}
}
