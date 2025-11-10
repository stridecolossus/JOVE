package org.sarge.jove.platform.obj;

import java.util.Scanner;

/**
 * A <i>parser</i> processes an OBJ command.
 * @author Sarge
 */
interface Parser {
	/**
	 * Parses the given command.
	 * @param scanner Input scanner
	 * @throws NumberFormatException if the data cannot be parsed
	 */
	void parse(Scanner scanner);

	/**
	 * Parser that ignores a command.
	 */
	Parser IGNORE = _ -> {
		// Does nowt
	};
}
