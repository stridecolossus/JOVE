package org.sarge.jove.control;

import java.time.Instant;

/**
 * A <i>frame listener</li> notifies completion of a rendered frame.
 * @author Sarge
 */
@FunctionalInterface
public interface FrameListener {
	/**
	 * Notifies a completed frame.
	 * @param start		Start time
	 * @param end		Completion time
	 */
	void frame(Instant start, Instant end);
}
