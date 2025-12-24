package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * The <i>render loop</i> executes rendering tasks sequentially on a single thread at a configured frame rate.
 * @author Sarge
 */
public class RenderLoop implements AutoCloseable {
	private final Runnable task;
	private final Frame.Tracker tracker;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> future;
	private Consumer<Throwable> handler = Throwable::printStackTrace;
	private int rate = 60;

	/**
	 * Constructor.
	 * @param task			Render task
	 * @param tracker		Frame tracker
	 */
	public RenderLoop(Runnable task, Frame.Tracker tracker) {
		this.task = requireNonNull(task);
		this.tracker = requireNonNull(tracker);
	}

	/**
	 * @return Whether this render loop is running
	 */
	public boolean isRunning() {
		return Objects.nonNull(future);
	}

	/**
	 * @return Target frame rate (or FPS)
	 */
	public int rate() {
		return rate;
	}

	/**
	 * Sets the target frame rate.
	 * @param rate Frame rate
	 * @throws IllegalArgumentException if {@link #rate} is not positive
	 * @throws IllegalStateException if this loop is running
	 */
	public void rate(int rate) {
		if(isRunning()) {
			throw new IllegalStateException("Cannot set frame rate while running");
		}
		this.rate = requireOneOrMore(rate);
	}

	/**
	 * Sets the handler for exceptions in the render task.
	 * Exceptions are dumped to the error console by default.
	 * @param handler Exception handler
	 */
	public void handler(Consumer<Throwable> handler) {
		this.handler = requireNonNull(handler);
	}

	/**
	 * Starts the render loop.
	 * @throws IllegalStateException if rendering has already been started
	 */
	public synchronized void start() {
		if(isRunning()) {
			throw new IllegalStateException("Render loop already running");
		}

		final long period = TimeUnit.SECONDS.toMicros(1) / rate;
		future = executor.scheduleAtFixedRate(this::run, 0, period, TimeUnit.MICROSECONDS);
	}

	/**
	 * Runs the task and notifies the elapsed duration.
	 */
	private void run() {
		try(final var _ = tracker.timer()) {
			task.run();
		}
		catch(Throwable e) {
			handler.accept(e);
		}
	}

	/**
	 * Stops the render loop.
	 * @throws IllegalStateException if rendering has not been started
	 */
	public synchronized void stop() {
		if(!isRunning()) {
			throw new IllegalStateException("Render loop has not been started");
		}
		future.cancel(true);
		future = null;
	}

	/**
	 * Toggles whether this render loop is paused.
	 * @param paused Whether paused
	 */
	public void pause(boolean paused) {
		if(paused) {
			if(isRunning()) {
				stop();
			}
		}
		else {
			start();
		}
	}
	// TODO - separate control class? +test

	@Override
	public synchronized void close() throws Exception {
		if(isRunning()) {
			stop();
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
	}
}
