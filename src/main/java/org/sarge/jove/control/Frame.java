package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.time.*;
import java.util.*;

/**
 * A <i>frame</i> records the elapsed duration of a frame rendering task.
 * @author Sarge
 */
public record Frame(Instant start, Instant end) {
	/**
	 * A <i>frame listener</i> is an observer for frame events.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies completion of a frame.
		 * @param frame Frame
		 */
		void end(Frame frame);

		/**
		 * Creates an adapter for a frame listener invoked on the expiry of the given duration.
		 * @param period		Iteration period
		 * @param delegate		Delegate listener
		 * @return Periodic listener
		 */
		static Listener periodic(Duration period, Listener delegate) {
			requireOneOrMore(period);
			requireNonNull(delegate);

			return new Listener() {
				private Instant end = Instant.now().plus(period);

				@Override
				public void end(Frame frame) {
					if(frame.end.isAfter(end)) {
						delegate.end(frame);
						end = frame.end.plus(period);
					}
				}
			};
		}
	}

	/**
	 * @return Elapsed duration of this frame
	 */
	public Duration elapsed() {
		return Duration.between(start, end);
	}

	/**
	 * The <i>frame tracker</i> measures frame durations and notifies listeners of frame events.
	 */
	public static class Tracker {
		private final Set<Listener> listeners = new HashSet<>();

		/**
		 * Starts a new frame.
		 * @return Frame completion callback
		 * @see Listener#start(int)
		 */
    	public Runnable begin() {
    		return new Timer();
    	}

    	/**
    	 * Notifies listeners on completion of the frame.
    	 */
    	private class Timer implements Runnable {
            private final Instant start = Instant.now();
            private boolean completed;

            @Override
            public void run() {
                if(completed) {
                	throw new IllegalStateException();
                }

                final Frame frame = new Frame(start, Instant.now());

                for(Listener listener : listeners) {
                	listener.end(frame);
                }

                completed = true;
            }
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
    }
}
