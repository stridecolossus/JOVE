package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.concurrent.*;

import org.sarge.lib.util.Check;

/**
 * The <i>render loop</i> schedules render tasks.
 * <p>
 * Usage:
 * <pre>
 * // Create render loop running at 50 frames-per-second
 * RenderLoop loop = new RenderLoop();
 * loop.rate(50);
 *
 * // Start render loop
 * RenderSequence seq = ...
 * FrameProcessor proc = ...
 * Runnable task = () -> proc.next().render(seq);
 * loop.start(task);
 *
 * ...
 *
 * // Stop rendering
 * loop.stop();
 * loop.close();
 * </pre>
 * <p>
 * @author Sarge
 */
public class RenderLoop {
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
	 * @param fps Frames-per-second
	 */
	public void rate(int fps) {
		check();
		this.rate = TimeUnit.SECONDS.toMillis(1) / fps;
	}

	/**
	 * @throws IllegalStateException if this loop is running
	 */
	private void check() {
		if(future != null) throw new IllegalStateException("Render loop has already been started");
	}

	/**
	 * Starts the render loop.
	 * @param task Render task
	 * @throws IllegalStateException if rendering has already been started
	 */
	public void start(Runnable task) {
		Check.notNull(task);
		check();
		future = executor.scheduleAtFixedRate(task, 0, rate, TimeUnit.MILLISECONDS);
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

	/**
	 * Terminates this render loop.
	 * @throws IllegalStateException if rendering has not been started
	 */
	public void close() {
		stop();
		executor.shutdownNow();
	}
}
