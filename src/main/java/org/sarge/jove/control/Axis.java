package org.sarge.jove.control;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event.Source;

/**
 * An <i>axis</i> generates events for an axial controller such as the mouse wheel or a HOTAS gauge.
 * @author Sarge
 */
public abstract class Axis implements Event, Source<Axis> {
	private float value;

	@Override
	public Object type() {
		return this;
	}

	/**
	 * @return Current axis position
	 */
	public float value() {
		return value;
	}

	/**
	 * Sets the value of this axis.
	 * @param value New value
	 */
	protected void update(float value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(value).build();
	}
}
