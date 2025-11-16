package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * The <i>render loop</i> performs frame rendering according to a configured frame rate.
 * <p>
 * Note that frame rendering tasks are executed sequentially on a single thread.
 * <p>
 * @author Sarge
 */
public class RenderLoop {
	/**
	 * Listener for frame events.
	 */
	public interface FrameListener {
		/**
		 * Notifies a completed frame.
		 * @param elapsed Elapsed duration
		 */
		void frame(Duration elapsed);
	}

	// Configuration
	private int rate;
	private Consumer<Exception> handler = Exception::printStackTrace;

	// Listeners
	private final Set<FrameListener> listeners = new HashSet<>();
	private final FrameCounter counter = new FrameCounter();

	// Scheduling
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private Runnable task;
	private Future<?> future;
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
		return Objects.nonNull(future);
	}

	/**
	 * @return Whether this render loop has been paused
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * @return FPS counter
	 */
	public FrameCounter counter() {
		return counter;
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
	public void handler(Consumer<Exception> handler) {
		this.handler = requireNonNull(handler);
	}

	/**
	 * Starts the render loop.
	 * @param task Render task
	 * @throws IllegalStateException if rendering has already been started
	 */
	public void start(Runnable task) {
		if(isRunning()) {
			throw new IllegalStateException("Render loop already running");
		}
		this.task = requireNonNull(task);
		schedule();
	}

	/**
	 * Starts or resumes scheduling of the render task.
	 */
	private void schedule() {
		if(future != null) {
			assert future.isCancelled();
		}
		final long period = TimeUnit.SECONDS.toMillis(1) / rate;
		future = executor.scheduleAtFixedRate(this::run, 0, period, TimeUnit.MILLISECONDS);
	}

	/**
	 * Runs the task and notifies the elapsed duration.
	 */
	private void run() {
		counter.start();
		try {
			task.run();
		}
		catch(Exception e) {
			handler.accept(e);
		}
		final Duration elapsed = counter.stop();
		update(elapsed);
	}

	/**
	 * Registers a frame completion listener.
	 * @param listener Listener to add
	 */
	public void add(FrameListener listener) {
		requireNonNull(listener);
		listeners.add(listener);
	}

	/**
	 * Detaches a frame completion listener.
	 * @param listener Listener to remove
	 */
	public void remove(FrameListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies listeners on a completed frame.
	 */
	private void update(Duration elapsed) {
		for(FrameListener listener : listeners) {
			listener.frame(elapsed);
		}
	}

	/**
	 * Pauses the render loop.
	 * @throws IllegalStateException if the loop is not running or is already paused
	 */
	public void pause() {
		if(!isRunning()) {
			throw new IllegalStateException("Render loop has not been started");
		}
		if(paused) {
			throw new IllegalStateException("Render loop is already paused");
		}
		future.cancel(true);
		paused = true;
	}

	/**
	 * Restarts a paused render loop.
	 * @throws IllegalStateException if the loop is not paused
	 */
	public void restart() {
		if(!paused) {
			throw new IllegalStateException("Render loop is not paused");
		}
		schedule();
		paused = false;
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
		paused = false;
	}
}
