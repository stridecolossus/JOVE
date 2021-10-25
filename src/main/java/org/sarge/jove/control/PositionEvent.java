package org.sarge.jove.control;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event.AbstractEvent;

/**
 * A <i>position event</i> describes a positional event such as the mouse pointer location.
 * @author Sarge
 */
public class PositionEvent extends AbstractEvent {
	public final float x, y;

	/**
	 * Constructor.
	 * @param type		Type of event
	 * @param src		Source
	 * @param x
	 * @param y
	 */
	public PositionEvent(Type type, Source src, float x, float y) {
		super(type, src);
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(super.toString())
				.append(String.format("%10.5f", x, y))
				.build();
	}
}
