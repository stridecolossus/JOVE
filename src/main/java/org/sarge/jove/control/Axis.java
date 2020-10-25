package org.sarge.jove.control;

import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.zeroOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.InputEvent.AbstractInputEventType;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> describes an input event for a controller axis or the mouse wheel.
 * @author Sarge
 */
public final class Axis extends AbstractInputEventType {
	private final String prefix;
	private final int id;

	/**
	 * Constructor.
	 * @param prefix	Name prefix
	 * @param id 		Axis identifier
	 */
	public Axis(String prefix, int id) {
		super(prefix, id);
		this.prefix = notEmpty(prefix);
		this.id = zeroOrMore(id);
	}

	/**
	 * @return Name prefix
	 */
	public String prefix() {
		return prefix;
	}

	/**
	 * @return Axis identifier
	 */
	public int id() {
		return id;
	}

	/**
	 * Creates an axis input event.
	 * @param value Axis value
	 * @return New axis event
	 */
	public Event create(float value) {
		return new Event(value);
	}

	@Override
	public Type parse(String[] tokens) {
		final String prefix = tokens[0];
		final int id = Integer.parseInt(tokens[1]);
		return new Axis(prefix, id);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return
					(obj instanceof Axis that) &&
					(this.id == that.id) &&
					this.prefix.equals(that.prefix);
		}
	}

	/**
	 * Axis event.
	 */
	public final class Event implements InputEvent {
		private final float value;

		/**
		 * Constructor.
		 * @param value Axis value
		 */
		private Event(float value) {
			this.value = value;
		}

		/**
		 * @return Axis value
		 */
		public float value() {
			return value;
		}

		@Override
		public Type type() {
			return Axis.this;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Event that) &&
					this.type().equals(that.type()) &&
					MathsUtil.isEqual(this.value, that.value);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("axis", name())
					.append("value", value)
					.build();
		}
	}
}
