package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.time.*;
import java.util.*;

/**
 * A <i>frame</i> records the elapsed duration of a frame rendering task.
 * @author Sarge
 */
public record Frame(Instant start, Instant end) {
	/**
	 * A <i>frame listener</i> is an observer for frame completion.
	 */
	public interface Listener {
		/**
		 * Notifies completion of a frame.
		 * @param frame Frame
		 */
		void update(Frame frame);
	}

	/**
	 * A <i>frame timer</i> measures the elapsed duration of a frame.
	 */
	public interface Timer {
		/**
		 * Completes this frame.
		 * @throws IllegalStateException if already completed
		 */
		void end();
	}

	/**
	 * @return Elapsed duration of this frame
	 */
	public Duration elapsed() {
		return Duration.between(start, end);
	}

	/**
	 * The <i>frame tracker</i> measures frame durations and notifies listeners of completed frames.
	 */
	public static class Tracker {
		private final Set<Listener> listeners = new HashSet<>();

		/**
		 * Starts a new frame measurement.
		 * @return Timer
		 */
    	public Timer begin() {
    		return new Timer() {
    			private final Instant start = Instant.now();
    			private boolean completed;

    			@Override
    			public void end() {
    				if(completed) throw new IllegalStateException();
    				completed = true;
    				update(new Frame(start, Instant.now()));
    			}
    		};
    	}

    	/**
    	 * Registers a frame completion listener.
    	 * @param listener Listener to add
    	 */
    	public void add(Listener listener) {
    		requireNonNull(listener);
    		listeners.add(listener);
    	}

    	/**
    	 * Detaches a frame completion listener.
    	 * @param listener Listener to remove
    	 */
    	public void remove(Listener listener) {
    		listeners.remove(listener);
    	}

    	/**
    	 * Removes all frame listeners.
    	 */
    	public void clear() {
    		listeners.clear();
    	}

    	/**
    	 * Notifies listeners on completion of a frame.
    	 */
    	private void update(Frame frame) {
    		for(Listener listener : listeners) {
    			listener.update(frame);
    		}
    	}
    }
}
