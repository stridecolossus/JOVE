package org.sarge.jove.control;

import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>position event</i> represents a 2D movement event such as the mouse pointer.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record PositionEvent(Source<PositionEvent> source, float x, float y) implements Event {
	/**
	 * Constructor.
	 * @param source Event source
	 * @param x
	 * @param y
	 */
	public PositionEvent {
		Check.notNull(source);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PositionEvent that) &&
				(this.source == that.source) &&
				MathsUtil.isEqual(this.x, that.x) &&
				MathsUtil.isEqual(this.y, that.y);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(source)
				.append(String.format("%f,%f", x, y))
				.build();
	}

	/**
	 * A <i>position event handler</i> abstracts a method that handles a position change.
	 */
	@FunctionalInterface
	public static interface Handler {
		/**
		 * Handles a position event.
		 */
		void handle(float x, float y);

		/**
		 * Creates an adapter for a position event consumer that delegates to the given handler.
		 * @param handler Position handler
		 * @return Position event adapter
		 */
		static Consumer<PositionEvent> adapter(Handler handler) {
			return pos -> handler.handle(pos.x, pos.y);
		}
	}
}
