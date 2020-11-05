package org.sarge.jove.control;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.util.Check.notEmpty;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>button</i> describes a keyboard or controller button.
 */
public final class Button implements Type {
	/**
	 * Button operations.
	 */
	public enum Operation {
		PRESS,
		RELEASE,
		REPEAT
	}

	private final String name;
	private final Map<Operation, Event> events = Arrays.stream(Operation.values()).map(Event::new).collect(toMap(Event::operation, Function.identity()));

	/**
	 * Constructor.
	 * @param name Button name
	 */
	public Button(String name) {
		this.name = notEmpty(name);
	}

	@Override
	public String name() {
		return name;
	}

	/**
	 * Creates the button event for the given operation.
	 * @param op Button operation
	 * @return Button event
	 */
	public Event event(Operation op) {
		return events.get(op);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Button.class, name);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return (obj instanceof Button that) && this.name.equals(that.name);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Button event.
	 */
	public final class Event implements InputEvent<Button> {
		private final Operation op;

		/**
		 * Constructor.
		 * @param op Button operation
		 */
		private Event(Operation op) {
			this.op = op;
		}

		/**
		 * @return Button operation
		 */
		public Operation operation() {
			return op;
		}

		@Override
		public Button type() {
			return Button.this;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Event that) &&
					(this.op == that.op) &&
					this.type().equals(that.type());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("name", type())
					.append("op", op)
					.build();
		}
	}
}
