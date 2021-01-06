package org.sarge.jove.platform.obj;

/**
 * Parser for a OBJ command line.
 */
public interface Parser {
	/**
	 * Parses the given arguments.
	 * @param args 		Arguments
	 * @param model		OBJ model
	 * @throws NumberFormatException is the data cannot be parsed
	 */
	void parse(String[] args, ObjectModel model);

	/**
	 * Parser that ignores the arguments.
	 */
	Parser IGNORE = (args, model) -> {
		// Does nowt
	};

	/**
	 * Parser for a new object command (either {@code o} or {@code g}).
	 * @see ObjectModel#start()
	 */
	Parser GROUP = (args, model) -> model.start();
}
