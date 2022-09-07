package org.sarge.jove.control;

import java.time.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>frame</i>
 * TODO
 * @author Sarge
 */
public interface Frame {
	/**
	 * A <i>frame listener</li> notifies completion of a rendered frame.
	 */
	@FunctionalInterface
	interface Listener {
		/**
		 * Notifies a completed frame.
		 */
		void update();
	}

	/**
	 * @return Time of last frame completion
	 */
	Instant time();

	/**
	 * @return Elapsed duration of this frame
	 */
	Duration elapsed();

	/**
	 * A <i>frame tracker</i> is a simple stopwatch timer for a frame.
	 */
	class Tracker implements Frame {
		private Instant start = Instant.EPOCH;
		private Instant end = Instant.EPOCH;
		private boolean running;

		@Override
		public Instant time() {
			return end;
		}

		@Override
		public Duration elapsed() {
			return Duration.between(start, end);
		}

		/**
		 * Starts a new frame.
		 * @throws IllegalStateException if this frame has already been started
		 */
		public Instant start() {
			if(running) throw new IllegalStateException();
			start = Instant.now();
			running = true;
			return start;
		}

		/**
		 * Ends this frame.
		 * @throws IllegalStateException if this frame has not been started
		 */
		public void end() {
			if(!running) throw new IllegalStateException();
			this.end = Instant.now();
			this.running = false;
		}

		@Override
		public String toString() {
			return String.format("%s -> %s", start, end);
		}
	}

	/**
	 * A <i>frame counter</i> tracks FPS (frames per second).
	 */
	class Counter implements Listener {
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
				next = now.plusSeconds(1);
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
