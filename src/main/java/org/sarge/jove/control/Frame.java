package org.sarge.jove.control;

import java.time.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>frame</i> tracks rendering duration.
 * @author Sarge
 */
public class Frame {
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

	private Instant start;
	private Instant end = Instant.EPOCH;
	private boolean running;

	/**
	 * @return Time of last frame completion
	 */
	public Instant time() {
		return end;
	}

	/**
	 * @return Elapsed duration
	 */
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

	/**
	 * A <i>frame counter</i> tracks FPS (frames per second).
	 */
	public static class Counter implements Frame.Listener {
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
