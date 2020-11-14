package org.sarge.jove.control;

import static org.sarge.jove.util.Check.notEmpty;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.InputEvent.Action;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> describes an input event for a controller axis or the mouse wheel.
 * @author Sarge
 */
public class Axis implements InputEvent.Type {
	/**
	 * Creates an adapter for an axis action that delegates to the given method.
	 * @param action Action method
	 * @return Axis action adapter
	 */
	public static Action<Axis> action(Consumer<Float> action) {
		return event -> action.accept(event.y());
	}

	private final String name;

	/**
	 * Constructor.
	 * @param name Axis name
	 */
	public Axis(String name) {
		this.name = notEmpty(name);
	}

	@Override
	public String name() {
		return name;
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
	public int hashCode() {
		return Objects.hash(Axis.class, name);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return (obj instanceof Axis that) && this.name.equals(that.name);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name).build();
	}

	/**
	 * Axis event.
	 */
	public final class Event implements InputEvent<Axis> {
		private final float value;

		/**
		 * Constructor.
		 * @param value Axis value
		 */
		private Event(float value) {
			this.value = value;
		}

		@Override
		public float x() {
			return value;
		}

		@Override
		public float y() {
			return value;
		}

		@Override
		public Axis type() {
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
