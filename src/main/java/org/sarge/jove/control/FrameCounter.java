package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.time.*;
import java.util.LinkedList;

/**
 * A <i>frame counter</i> tracks the number of completed frames over a given period.
 * @author Sarge
 */
public class FrameCounter implements Frame.Listener {
	private final Duration window;
	private final LinkedList<Instant> history = new LinkedList<>();

	/**
	 * Default constructor for a FPS counter.
	 */
	public FrameCounter() {
		this(Duration.ofSeconds(1));
	}

	/**
	 * Constructor.
	 * @param window Window duration
	 */
	public FrameCounter(Duration window) {
		this.window = requireNonNull(window);
	}

	/**
	 * @return Current frame count
	 */
	public int count() {
		return history.size();
	}

	@Override
	public void frame(Frame frame) {
		clean(frame.end());
		history.add(frame.start());
	}

	/**
	 * Culls time measurements older than the configured window.
	 * @param time Latest frame time
	 */
	private void clean(Instant time) {
		final Instant latest = time.minus(window);
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
		return String.format("FrameCounter[count=%d period=%s]", history.size(), window);
	}
}
