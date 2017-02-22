package org.sarge.jove.model.obj;

import java.util.Scanner;

/**
 * Use material line parser.
 * @author Sarge
 */
public class UseMaterialParser implements Parser {
	@Override
	public void parse(Scanner scanner, ObjectModel data) {
		// Lookup material
		final String name = scanner.next();
		final ObjectMaterial mat = data.getMaterial(name);
		if(mat == null) throw new IllegalArgumentException("Unknown material: " + name);

		// Add material to current node
		data.getGroup().setMaterial(mat);
	}
}
