package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>button</i> defines a toggle event such as a keyboard key or mouse button.
 * <p>
 * A button has an {@link Action} and optionally a keyboard {@link Modifier} set.
 * <p>
 * Note that a button is both the event <b>and</b> its {@link #type()}.
 * <p>
 * @author Sarge
 */
public class Button implements Event {
	/**
	 * Button name delimiter.
	 */
	private static final String DELIMITER = "-";

	/**
	 * Button actions.
	 */
	public enum Action {
		RELEASE,
		PRESS,
		REPEAT
	}

	/**
	 * Button modifiers.
	 */
	public enum Modifier implements IntegerEnumeration {
		SHIFT(0x0001),
		CONTROL(0x0002),
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

	private final String id;
	private final String name;
	private final Action action;
	private final int mods;

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param action		Action
	 * @param mods			Modifier mask
	 */
	public Button(String id, Action action, int mods) {
		this.id = notEmpty(id);
		this.action = notNull(action);
		this.mods = zeroOrMore(mods);
		this.name = build();
	}

	/**
	 * Constructor for a basic button.
	 * @param id Button identifier
	 */
	public Button(String id) {
		this(id, Action.PRESS, 0);
	}

	/**
	 * Builds the button name.
	 */
	private String build() {
		final String modifiers = name(modifiers().toArray());
		return name(id, action.name(), modifiers);
	}

	/**
	 * Builds a hyphen delimited name from the given tokens.
	 * @param tokens Tokens
	 * @return Button name
	 */
	public static String name(Object... tokens) {
		return Arrays
				.stream(tokens)
				.map(String::valueOf)
				.collect(joining(DELIMITER));
	}

	/**
	 * @return Button ID
	 */
	public String id() {
		return id;
	}

	/**
	 * @return Button name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Button action
	 */
	public Action action() {
		return action;
	}

	/**
	 * @return Key modifiers
	 */
	public Set<Modifier> modifiers() {
		return IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
	}

	@Override
	public Object type() {
		return this;
	}

	/**
	 * Creates this button with the given actions and modifier mask.
	 * @param action		Action
	 * @param mods			Modifier mask
	 * @return New button
	 */
	public Button resolve(Action action, int mods) {
		return new Button(id, action, mods);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, action, mods);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Button that) &&
				this.id.equals(that.id) &&
				(this.action == that.action) &&
				(this.mods == that.mods);
	}

	@Override
	public String toString() {
		return name;
	}
}
