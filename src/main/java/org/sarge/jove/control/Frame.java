package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

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
		void frame();
	}

	private Instant start;
	private Duration elapsed;
	private boolean running;

	/**
	 * @return Start time
	 */
	public Instant time() {
		return start;
	}

	/**
	 * @return Elapsed duration
	 */
	public Duration elapsed() {
		return elapsed;
	}

	/**
	 * Starts a new frame.
	 * @throws IllegalStateException if this frame has already been started
	 */
	public void start() {
		if(running) throw new IllegalStateException();
		start = Instant.now();
		running = true;
	}

	/**
	 * Ends this frame.
	 * @return Elapsed duration
	 * @throws IllegalStateException if this frame has not been started
	 */
	public Duration end() {
		end(Duration.between(start, Instant.now()));
		return elapsed;
	}

	/**
	 * Ends this frame.
	 * @param elapsed Elapsed duration
	 * @throws IllegalStateException if this frame has not been started
	 */
	protected void end(Duration elapsed) {
		if(!running) throw new IllegalStateException();
		this.elapsed = notNull(elapsed);
		this.running = false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(start).append(elapsed).toString();
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
		public void frame() {
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
