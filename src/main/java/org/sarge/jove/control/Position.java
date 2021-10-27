package org.sarge.jove.control;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.control.Position.PositionEvent;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>position</i> defines a positional event such as the mouse pointer location.
 * @author Sarge
 */
public record Position(String name, Source source) implements Type<PositionEvent> {
	/**
	 * Adapter for a position handler method.
	 */
	@FunctionalInterface
	public interface PositionHandler {
		/**
		 * Handles a position event.
		 * @param x
		 * @param y
		 */
		void handle(float x, float y);
	}

	/**
	 * Constructor.
	 * @param name
	 * @param source
	 */
	public Position {
		Check.notEmpty(name);
		Check.notNull(source);
	}

	/**
	 * Position event.
	 */
	public class PositionEvent implements Event {
		public final float x, y;

		/**
		 * Constructor.
		 * @param x
		 * @param y
		 */
		public PositionEvent(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public Type<?> type() {
			return Position.this;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof PositionEvent that) &&
					this.type().equals(that.type()) &&
					MathsUtil.isEqual(this.x, that.x) &&
					MathsUtil.isEqual(this.y, that.y);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(name)
					.append(String.format("%f,%f", x, y))
					.build();
		}
	}
}
