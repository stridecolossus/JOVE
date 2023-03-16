package org.sarge.jove.control;

import java.time.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>frame timer</i> is a simple stopwatch for the elapsed duration of a rendered frame.
 * @author Sarge
 */
public class FrameTimer {
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
		void update(FrameTimer frame);
	}

	private Instant start = Instant.now();
	private Instant end;

	/**
	 * @return Frame completion time
	 * @throws IllegalStateException if this frame has not been completed
	 */
	public Instant time() {
		check();
		return end;
	}

	/**
	 * @return Elapsed duration of this frame
	 * @throws IllegalStateException if this frame has not been completed
	 */
	public Duration elapsed() {
		check();
		return Duration.between(start, end);
	}

	/**
	 * @throws IllegalStateException if this frame has not been completed
	 */
	private void check() {
		if(end == null) throw new IllegalStateException("Frame has not been completed");
	}

	/**
	 * Stops this frame.
	 * @throws IllegalStateException if this frame has already been completed
	 */
	public void stop() {
		if(end != null) throw new IllegalStateException("Frame has already been completed");
		end = Instant.now();
	}

	@Override
	public String toString() {
		return String.format("%s -> %s", start, end);
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
		public void update(FrameTimer frame) {
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
