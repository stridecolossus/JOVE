package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.function.Consumer;

import org.sarge.jove.control.Event.Source;
import org.sarge.lib.util.Check;

/**
 * A <i>button</i> is an event for a keyboard, mouse, or joystick button.
 * @author Sarge
 */
public record Button(Source<Button> source, String id, int action, int modifiers) implements Event {
	/**
	 * Button name delimiter.
	 */
	private static final String DELIMITER = "-";

	/**
	 * Constructor.
	 * @param source			Event source
	 * @param id				Button identifier
	 * @param action			Action code
	 * @param modifiers			Keyboard modifiers bit-field
	 */
	public Button {
		Check.notNull(source);
		Check.notEmpty(id);
		Check.zeroOrMore(action);
		Check.zeroOrMore(modifiers);
	}

//	/**
//	 * Default button actions.
//	 */
//	public enum Action {
//		RELEASE,
//		PRESS,
//		REPEAT;
//
//		private static final Action[] ACTIONS = Action.values();
//
//		/**
//		 * Maps an action code to this enumeration.
//		 * @param action Action code
//		 * @return Action
//		 * @throws ArrayIndexOutOfBoundsException for an invalid action code
//		 */
//		public static Action map(int action) {
//			return ACTIONS[action];
//		}
//	}

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

	public static Consumer<Button> handler(Runnable action) {
		return button -> action.run();
	}
}
