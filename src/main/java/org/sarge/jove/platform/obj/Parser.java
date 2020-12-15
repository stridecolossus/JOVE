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
	 * Parser for a group command (either <b>g</b> or <b>s</b>, arguments are ignored).
	 * @see ObjectModel#start()
	 */
	Parser GROUP = (args, model) -> model.start();
}
