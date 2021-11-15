package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.Set;

import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * A <i>button</i> defines a toggle event such as a keyboard key or mouse button.
 * <p>
 * A button has an {@link Action} and optionally a keyboard {@link Modifier} set.
 * <p>
 * The {@link #name()} method generates a human-readable string representation of the button, action and modifiers, see {@link Event#name(String...)}.
 * <p>
 * Note that a button is both the event <b>and</b> its type.
 * <p>
 * Usage:
 * <pre>
 * 	// Define button
 * 	Source source = ...
 * 	Button button = new Button("Name", source);
 *
 * 	// Derive button
 * 	Button derived = button.resolve(Action.RELEASE, Set.of(Modifier.SHIFT, Modifier.CONTROL));
 * </pre>
 * <p>
 * @author Sarge
 */
@SuppressWarnings("unused")
public record Button(String id, Source source, Action action, int mods) implements Type<Button>, Event {
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

	/**
	 * Constructor.
	 * @param id			Button identifier
	 * @param source		Event source
	 * @param action		Action
	 * @param mods			Modifier mask
	 */
	public Button {
		Check.notEmpty(id);
		Check.notNull(source);
		Check.notNull(action);
		Check.zeroOrMore(mods);
	}

	/**
	 * Constructor for a basic button.
	 * @param id			Button identifier
	 * @param source		Event source
	 */
	public Button(String id, Source source) {
		this(id, source, Action.PRESS, 0);
	}

	/**
	 * Creates this button with the given actions and modifier mask.
	 * @param action		Action
	 * @param mods			Modifier mask
	 * @return New button
	 */
	public Button resolve(Action action, int mods) {
		return new Button(id, source, action, mods);
	}

	@Override
	public String name() {
		final String modifiers = modifiers().stream().map(Enum::name).collect(joining(Event.DELIMITER));
		return Event.name(id, action.name(), modifiers);
	}

	/**
	 * @return Key modifiers
	 */
	public Set<Modifier> modifiers() {
		return IntegerEnumeration.mapping(Modifier.class).enumerate(mods);
	}

	@Override
	public final Type<?> type() {
		return this;
	}

	@Override
	public String toString() {
		return name();
	}
}
