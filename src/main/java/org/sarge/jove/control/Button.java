package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notEmpty;

import java.util.Arrays;
import java.util.Objects;

/**
 * A <i>button</i> defines a toggle event such as a keyboard key or mouse button.
 * <p>
 * Note that a button is both the event <b>and</b> its {@link #type()}.
 * <p>
 * A button can be treated as a <i>template</i> for the purposes of event matching using the {@link #matches(Button)} method.
 * <p>
 * The {@link #resolve(int)} method is used to <i>resolve</i> a button to a different action.
 * <p>
 * @author Sarge
 */
public interface Button extends Event {
	/**
	 * Button name delimiter.
	 */
	String DELIMITER = "-";

	/**
	 * @return Button identifier
	 */
	String id();

	/**
	 * @return Button name
	 */
	String name();

	/**
	 * @return Button action
	 */
	Object action();

	/**
	 * Matches the given button against this template.
	 * @param button Button
	 * @return Whether matches this template
	 */
	boolean matches(Button button);

	/**
	 * Resolves this button to the given action.
	 * @param action Action
	 * @return Resolved button
	 */
	Button resolve(int action);

	/**
	 * Builds a hyphen delimited name from the given tokens.
	 * @param tokens Tokens
	 * @return Button name
	 */
	static String name(Object... tokens) {
		return Arrays
				.stream(tokens)
				.map(String::valueOf)
				.collect(joining(DELIMITER));
	}

	/**
	 * A <i>toggle handler</i> defines the signature for a method bound to a toggled button.
	 */
	@FunctionalInterface
	interface ToggleHandler {
		/**
		 * Handles a toggle button event.
		 * @param pressed Whether the button is pressed
		 */
		void handle(boolean pressed);
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractButton implements Button {
		protected final String id;

		/**
		 * Constructor.
		 * @param id Button identifier
		 */
		protected AbstractButton(String id) {
			this.id = notEmpty(id);
		}

		@Override
		public final String id() {
			return id;
		}

		@Override
		public final Object type() {
			return this;
		}

		@Override
		public boolean matches(Button button) {
			return id.equals(button.id());
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
					this.name().equals(that.name());
		}

		@Override
		public String toString() {
			return name();
		}
	}
}
