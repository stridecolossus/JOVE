package org.sarge.jove.model.obj;

import java.io.IOException;
import java.util.Scanner;

/**
 * <tt>OBJ</tt> model parser.
 * @author Sarge
 */
public interface Parser {
	/**
	 * Parses an OBJ command.
	 * @param scanner		Input
	 * @param model			Model
	 * @throws IOException if the input cannot be parsed
	 */
	void parse(Scanner scanner, ObjectModel model) throws IOException;

	/**
	 * Parser that skips content, e.g. comments.
	 */
	Parser IGNORE_PARSER = (scanner, model) -> {
		// Ignored.
	};
}
