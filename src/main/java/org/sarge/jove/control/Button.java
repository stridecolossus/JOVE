package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.range;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>button</i> describes a keyboard or controller button.
 */
public final class Button implements Type, InputEvent {
	/**
	 * Button operations.
	 */
	public enum Operation {
		RELEASE,
		PRESS,
		REPEAT;

		private static final Operation[] OPERATIONS = Operation.values();
	}

	/**
	 * Button modifiers.
	 */
	public enum Modifier implements IntegerEnumeration {
		SHIFT(0x0001),
		CONTROL(0x002),
		ALT(0x0004),
		SUPER(0x0008),
		CAPS_LOCK(0x0010),
		NUM_LOCK(0x0020);

		private final int value;

		private Modifier(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	/**
	 * Parses a button from its string representation.
	 * @param name Button name
	 * @return New button
	 * @see #name()
	 * @throws IllegalArgumentException if the button cannot be parsed
	 */
	public static Button parse(String name) {
		// Tokenize name
		final String[] tokens = name.split(DELIMITER);

		// Start builder
		final Builder builder = new Builder();
		builder.id(tokens[0]);

		// Add operation
		if(tokens.length > 1) {
			final Operation op = Operation.valueOf(tokens[1]);
			builder.operation(op);
		}

		// Add modifiers
		for(int n = 2; n < tokens.length; ++n) {
			final Modifier mod = Modifier.valueOf(tokens[n]);
			builder.modifier(mod);
		}

		// Create button
		return builder.build();
	}

	/**
	 * Creates a button with the given identifier.
	 * @param id Button identifier
	 * @return New button
	 */
	public static Button of(String id) {
		return new Button(id, 1, 0);
	}

	private final String id;
	private final int action;
	private final int mods;

	/**
	 * Constructor.
	 * @param id		Button identifier
	 * @param op		Action 0..2
	 * @param mods		Modifiers bit-mask
	 */
	public Button(String id, int action, int mods) {
		this.id = notEmpty(id);
		this.action = range(action, 0, 2);
		this.mods = zeroOrMore(mods);
	}

	/**
	 * @return Button identifier
	 */
	public String id() {
		return id;
	}

	@Override
	public String name() {
		final StringJoiner str = new StringJoiner(DELIMITER);
		str.add(id);
		str.add(operation().name());
		if(mods > 0) {
			str.add(modifiers().stream().map(Enum::name).collect(joining(DELIMITER)));
		}
		return str.toString();
	}

	/**
	 * @return Button operation
	 */
	public Operation operation() {
		return Operation.OPERATIONS[action];
	}

	/**
	 * @return Button modifiers
	 */
	public Set<Modifier> modifiers() {
		return IntegerEnumeration.enumerate(Modifier.class, mods);
	}

	@Override
	public Button type() {
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, action, mods);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return
					(obj instanceof Button that) &&
					(this.action == that.action) &&
					(this.mods == that.mods);
		}
	}

	@Override
	public String toString() {
		return name();
	}

	/**
	 * Builder for a button.
	 */
	public static class Builder {
		private String id;
		private int action;
		private final Set<Modifier> mods = new HashSet<>();

		/**
		 * Sets the button identifier.
		 * @param id Button identifier
		 */
		public Builder id(String id) {
			this.id = notEmpty(id);
			return this;
		}

		/**
		 * Sets the button operation.
		 * @param op Button operation
		 */
		public Builder operation(Operation op) {
			this.action = op.ordinal();
			return this;
		}

		/**
		 * Adds a button modifier.
		 * @param mod Button modifier
		 */
		public Builder modifier(Modifier mod) {
			this.mods.add(mod);
			return this;
		}

		/**
		 * Constructs this button.
		 * @return New button
		 */
		public Button build() {
			final int mask = IntegerEnumeration.mask(mods);
			return new Button(id, action, mask);
		}
	}
}
