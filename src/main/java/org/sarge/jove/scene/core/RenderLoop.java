package org.sarge.jove.scene.core;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.concurrent.*;

import org.sarge.jove.control.FrameTimer;
import org.sarge.jove.control.FrameTimer.Listener;
import org.sarge.lib.util.Check;

/**
 * The <i>render loop</i> performs frame rendering according to a configured {@link Scheduler} policy.
 * <p>
 * Note that frame rendering tasks are executed sequentially on a single thread.
 * <p>
 * @see FrameTimer.Listener
 * @author Sarge
 */
public class RenderLoop {
	/**
	 * A <i>render loop</i> scheduler is responsible for scheduling render tasks.
	 */
	public interface Scheduler {
		/**
		 * Starts a render task.
		 * @param task Render task to start
		 * @return Future
		 */
		Future<?> start(Runnable task);

		/**
		 * A <i>continual</i> scheduler renders frames continually without throttling, i.e. essentially an infinite loop.
		 * @return Continual scheduler
		 */
		Scheduler CONTINUAL = task -> {
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			return executor.submit(task);
		};

		/**
		 * Creates a scheduler that renders at a fixed frame-rate.
		 * <p>
		 * Note that frames that take longer than the configured rate simply back up the rendering process.
		 * i.e. Frames may start late but are not executed concurrently.
		 * <p>
		 * @param fps Target frames-per-second
		 * @return Fixed rate scheduler
		 */
		static Scheduler fixed(int fps) {
			return task -> {
				final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
				final long period = TimeUnit.SECONDS.toMillis(1) / fps;
				return executor.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
			};
		}
	}

	private final Scheduler scheduler;
	private final Set<Listener> listeners = new HashSet<>();
	private Future<?> future;

	/**
	 * Constructor.
	 * @param scheduler Render task scheduler
	 */
	public RenderLoop(Scheduler scheduler) {
		this.scheduler = notNull(scheduler);
	}

	/**
	 * @return Whether this render loop is running
	 */
	public boolean isRunning() {
		return future != null;
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
		if(future != null) {
			throw new IllegalStateException("Render loop has already been started");
		}

		final Runnable wrapper = () -> {
			final FrameTimer frame = run(task);
			update(frame);
		};

		future = scheduler.start(wrapper);

		if(future == null) {
			throw new RuntimeException("Scheduler returned empty future: " + scheduler);
		}
	}

	/**
	 * Runs the given render task and tracks the elapsed duration.
	 * @param task Render task
	 * @return Frame
	 */
	private static FrameTimer run(Runnable task) {
		final FrameTimer timer = new FrameTimer();
		task.run();
		timer.stop();
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
		if(future == null) {
			throw new IllegalStateException("Render loop has not been started");
		}
		future.cancel(false);
		future = null;
	}
}
