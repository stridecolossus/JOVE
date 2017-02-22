package org.sarge.jove.model.obj;

import java.util.Scanner;

import org.sarge.jove.common.Colour;
import org.sarge.lib.util.Check;

/**
 * Parser for material colours.
 * @author Sarge
 */
public class ColourParser implements Parser {
	private final String name;
	private final float[] array = new float[4];

	/**
	 * Constructor.
	 * @param name Colour parameter name
	 */
	public ColourParser(String name) {
		Check.notEmpty(name);
		this.name = name;
	}

	@Override
	public void parse(Scanner scanner, ObjectModel model) {
		// Load RGB components
		for(int n = 0; n < 3; ++n) {
			array[n] = scanner.nextFloat();
		}
		
		// Load optional alpha component
		if(scanner.hasNextFloat()) {
			array[3] = scanner.nextFloat();
		}
		else {
			array[3] = 1;
		}

		// Add to material
		model.getMaterial().addColour(name, new Colour(array));
	}
}
