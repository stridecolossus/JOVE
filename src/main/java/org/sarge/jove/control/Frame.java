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
		void frame(Frame frame);

		// TODO - this stinks
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
				public void frame(Frame frame) {
					if(frame.end.isAfter(end)) {
						delegate.frame(frame);
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
		 * Starts a frame timer that notifies listeners on completion of the frame.
		 * @return Frame timer
		 * @see Listener#frame(Frame)
		 */
    	public AutoCloseable timer() {
    		return new AutoCloseable() {
    			private final Instant start = Instant.now();

    			@Override
    			public void close() throws Exception {
    				final Frame frame = new Frame(start, Instant.now());
		            for(Listener listener : listeners) {
		            	listener.frame(frame);
		            }
		    	}
    		};
    	}
    	// TODO - do we need the framebuffer index here? and if so HOW? since Runnable obviously does not return anything!

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
