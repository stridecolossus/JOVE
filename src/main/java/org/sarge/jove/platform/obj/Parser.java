package org.sarge.jove.platform.obj;

/**
 * A <i>parser</i> processes an OBJ command.
 * @author Sarge
 */
interface Parser {
	/**
	 * Parses the given command.
	 * @param tokens Command tokens
	 * @throws NumberFormatException if the data cannot be parsed
	 */
	void parse(String[] tokens);

	/**
	 * Parser that ignores a command line.
	 */
	Parser IGNORE = _ -> {
		// Ignored
	};
}
