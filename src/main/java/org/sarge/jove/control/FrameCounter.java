package org.sarge.jove.control;

import java.time.*;
import java.util.LinkedList;

/**
 * A <i>frame counter</i> tracks the number of completed frames over the last second.
 * @author Sarge
 */
public class FrameCounter {
	private final LinkedList<Instant> history = new LinkedList<>();
	private Instant start;

	/**
	 * @return Current frame count
	 */
	public int count() {
		clean();
		return history.size();
	}

	/**
	 * Culls time measurements older than one second.
	 */
	private void clean() {
		final Instant latest = Instant.now().minus(Duration.ofSeconds(1));
		while(!history.isEmpty()) {
			if(history.peek().isBefore(latest)) {
				history.removeFirst();
			}
			else {
				break;
			}
		}
	}

	/**
	 * Starts a frame.
	 * @throws IllegalStateException if already started
	 */
	public void start() {
		if(start != null) throw new IllegalStateException();
		start = Instant.now();
	}

	/**
	 * Stops the current frame.
	 * @throws IllegalStateException if not started
	 * @return Elapsed duration
	 */
	public Duration stop() {
		if(start == null) throw new IllegalStateException();

		final Duration elapsed = Duration.between(start, Instant.now());

		history.add(start);
		start = null;

		return elapsed;
	}

	@Override
	public String toString() {
		return String.format("FrameCounter[fps=%d]", count());
	}
}
