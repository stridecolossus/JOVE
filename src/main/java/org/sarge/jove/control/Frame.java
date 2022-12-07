package org.sarge.jove.control;

import java.time.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>frame</i> is a stopwatch timer for an elapsed duration.
 * @author Sarge
 */
public class Frame {
	/**
	 * Number of milliseconds per second.
	 */
	public static final long MILLISECONDS_PER_SECOND = TimeUnit.SECONDS.toMillis(1);

	/**
	 * A <i>frame listener</li> notifies completion of a rendered frame.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies a completed frame.
		 * @param frame Completed frame
		 */
		void update(Frame frame);
	}

	private Instant start = Instant.EPOCH;
	private Instant end = Instant.EPOCH;
	private Duration elapsed;

	/**
	 * @return Time of the last frame completion
	 */
	public Instant time() {
		check();
		return end;
	}

	/**
	 * @return Elapsed duration of this frame
	 */
	public Duration elapsed() {
		check();
		if(elapsed == null) {
			elapsed = Duration.between(start, end);
		}
		return elapsed;
	}

	/**
	 * Starts a new frame.
	 * @throws IllegalStateException if this frame has already been started
	 */
	public void start() {
		if(end == null) throw new IllegalStateException("Frame is already started");
		start = Instant.now();
		end = null;
		elapsed = null;
	}

	/**
	 * Stops this frame.
	 * @throws IllegalStateException if this frame has not been completed
	 */
	public void stop() {
		if(end != null) throw new IllegalStateException("Frame is already completed");
		end = Instant.now();
	}

	/**
	 * @throws IllegalStateException if this frame has not been completed
	 */
	private void check() {
		if(end == null) throw new IllegalStateException("Frame has not been completed");
	}

	@Override
	public String toString() {
		return String.format("%s -> %s (%s)", start, end, elapsed);
	}

	/**
	 * A <i>frame counter</i> tracks FPS (frames per second).
	 */
	public static class Counter implements Listener {
		private Instant next = Instant.EPOCH;
		private int count;

		/**
		 * @return Frames-per-second
		 */
		public int fps() {
			return count;
		}

		@Override
		public void update(Frame frame) {
			final Instant now = frame.time();
			if(now.isAfter(next)) {
				count = 1;
				next = now.plusMillis(MILLISECONDS_PER_SECOND);
			}
			else {
				++count;
			}
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("count", count).build();
		}
	}
}
