package org.sarge.jove.model.obj;

import java.util.Scanner;

import org.sarge.lib.util.Check;

/**
 * Parser for material texture maps.
 * @author Sarge
 */
public class TextureParser implements Parser {
	private final String name;

	/**
	 * Constructor.
	 * @param name Texture parameter name
	 */
	public TextureParser(String name) {
		Check.notEmpty(name);
		this.name = name;
	}
	
	@Override
	public void parse(Scanner scanner, ObjectModel model) {
		final String path = scanner.nextLine();
		model.getMaterial().addTexture(name, path);
	}
}
