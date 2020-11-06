package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.control.InputEvent.Type;

/**
 * A <i>button</i> describes a keyboard or controller button.
 */
public final class Button implements Type, InputEvent<Button> {
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

	// TODO - parse/of

	private final String id;
	private final Operation op;
	private final int mods;

	/**
	 * Constructor.
	 * @param id		Button identifier
	 * @param op		Operation 0..2
	 * @param mods		Modifiers bit-mask
	 */
	public Button(String id, int op, int mods) {
		this.id = notEmpty(id);
		this.op = Operation.OPERATIONS[op];
		this.mods = zeroOrMore(mods);
	}

	/**
	 * Convenience constructor for a button with the {@link Operation#PRESS} operation and no modifiers.
	 * @param id Button identifier
	 */
	public Button(String id) {
		this(id, 1, 0);
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
		if(mods > 0) {
			str.add(modifiers().stream().map(Enum::name).collect(joining(DELIMITER)));
		}
		str.add(op.name());
		return str.toString();
	}

	/**
	 * @return Button operation
	 */
	public Operation operation() {
		return op;
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
		return Objects.hash(id, op, mods);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return
					(obj instanceof Button that) &&
					(this.op == that.op) &&
					(this.mods == that.mods);
		}
	}

	@Override
	public String toString() {
		return name();
	}
}
