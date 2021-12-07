package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A <i>button</i> defines a toggle event such as a keyboard key or mouse button.
 * <p>
 * Note that a button is both the event <b>and</b> its {@link #type()}.
 * <p>
 * @author Sarge
 */
public interface Button extends Event {
	/**
	 * Button name delimiter.
	 */
	String DELIMITER = "-";

	/**
	 * @return Button name
	 */
	String name();

	/**
	 * @return Button action
	 */
	Object action();

	/**
	 * Builds a hyphen delimited name from the given tokens.
	 * @param tokens Tokens
	 * @return Button name
	 */
	static String name(Object... tokens) {
		return Arrays
				.stream(tokens)
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.filter(Predicate.not(String::isEmpty))
				.collect(joining(DELIMITER));
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractButton implements Button {
		@Override
		public final Object type() {
			return this;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Button that) &&
					this.action().equals(that.action()) &&
					this.name().equals(that.name());
		}

		@Override
		public String toString() {
			return name();
		}
	}
}
