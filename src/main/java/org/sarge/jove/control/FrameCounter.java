package org.sarge.jove.control;

import java.time.*;
import java.util.LinkedList;

/**
 * A <i>frame counter</i> tracks the number of completed frames over the last second.
 * @author Sarge
 */
public class FrameCounter implements Frame.Listener {
	private final LinkedList<Instant> history = new LinkedList<>();

	/**
	 * @return Current frame count
	 */
	public int count() {
		return history.size();
	}

	@Override
	public void update(Frame frame) {
		clean(frame.end());
		history.add(frame.start());
	}

	/**
	 * Culls time measurements older than one second.
	 * @param time Latest frame time
	 */
	private void clean(Instant time) {
		final Instant latest = time.minus(Duration.ofSeconds(1));
		while(true) {
			// Stop if empty
			if(history.isEmpty()) {
				break;
			}

			// Stop if within one second
			if(history.peek().isAfter(latest)) {
				break;
			}

			// Cull stale entry
			history.removeFirst();
		}
	}

	@Override
	public String toString() {
		return String.format("FrameCounter[fps=%d]", count());
	}
}
