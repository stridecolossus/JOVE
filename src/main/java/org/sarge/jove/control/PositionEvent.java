package org.sarge.jove.control;

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
	 * A <i>position handler</i> defines the signature for an action handler method that can be bound to position events.
	 */
	@FunctionalInterface
	public interface Handler {
		/**
		 * Handles a position event.
		 * @param x
		 * @param y
		 */
		void handle(float x, float y);
	}

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
	public Object type() {
		return source;
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
				.append(String.format("%f,%f", x, y))
				.build();
	}
}
