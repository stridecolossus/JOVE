package org.sarge.jove.platform.obj;

/**
 * A <i>parser</i> handles an OBJ command line.
 * @author Sarge
 */
public interface Parser {
	/**
	 * Parses the given arguments.
	 * @param args 		Arguments
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

		// TODO - models indexed by name, return map on build

		// Start new object
		model.start();

		// Init object name
		if(args.length > 0) {
			model.name(args[0].trim());
		}
	};
}
