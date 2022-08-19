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
		 * @param frame Completed frame
		 */
		void completed(Frame frame);
	}

	private final Instant start = Instant.now();
	private Duration elapsed;

	/**
	 * @return Frame start time
	 */
	public Instant start() {
		return start;
	}

	/**
	 * @return Elapsed duration
	 */
	public Duration elapsed() {
		return elapsed;
	}

	/**
	 * Ends this frame.
	 * @throws IllegalStateException if this frame has not been started
	 */
	public void end() {
		end(Duration.between(start, Instant.now()));
	}

	/**
	 * Ends this frame.
	 * @param elapsed Elapsed duration
	 * @throws IllegalStateException if this frame has not been started
	 * @throws IllegalArgumentException if the given duration is negative
	 */
	public void end(Duration elapsed) {
		if(this.elapsed != null) throw new IllegalStateException("Frame already completed: " + this);
		if(elapsed.isNegative()) throw new IllegalArgumentException("Elapsed time cannot be negative");
		this.elapsed = notNull(elapsed);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(start).append(elapsed).toString();
	}
}
