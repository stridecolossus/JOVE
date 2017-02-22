package org.sarge.jove.model.obj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Material library file-path parser.
 * @author Sarge
 */
public class MaterialLibraryParser implements Parser {
	private final Map<String, Parser> parsers;
	
	/**
	 * Constructor.
	 * @param parsers Material parsers
	 */
	public MaterialLibraryParser(Map<String, Parser> parsers) {
		this.parsers = new HashMap<>(parsers);
	}
	
	@Override
	public void parse(Scanner scanner, ObjectModel model) throws IOException {
		final String path = scanner.nextLine();
		ObjectModelLoader.load(path, parsers, model);
	}
}
