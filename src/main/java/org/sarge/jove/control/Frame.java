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
		 */
		void update();
	}

	private Instant start = Instant.EPOCH;
	private Instant end = Instant.EPOCH;
	private Duration elapsed;

	/**
	 * @return Time of last frame completion
	 */
	public Instant time() {
		return end;
	}

	/**
	 * @return Elapsed duration of this frame
	 */
	public Duration elapsed() {
		if(elapsed == null) {
			end = Instant.now();
			elapsed = Duration.between(start, end);
		}
		return elapsed;
	}

	/**
	 * Starts a new frame.
	 */
	public Instant start() {
		start = Instant.now();
		elapsed = null;
		return start;
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
		public void update() {
			final Instant now = Instant.now();
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
