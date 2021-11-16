package org.sarge.jove.platform.obj;

/**
 * A <i>parser</i> handles an OBJ command.
 * @author Sarge
 */
public interface Parser {
	/**
	 * Parses the given command.
	 * @param args 		Arguments (including the command token)
	 * @param model		OBJ model
	 * @throws NumberFormatException if the data cannot be parsed
	 */
	void parse(String[] args, ObjectModel model);

	/**
	 * Parser that ignores a command.
	 */
	Parser IGNORE = (args, model) -> {
		// Does nowt
	};

	/**
	 * Parser for a new object command (either {@code o} or {@code g}).
	 * @see ObjectModel#start()
	 */
	Parser GROUP = (args, model) -> {
		// TODO - object names
		// Start new object
		model.start();
	};
}
