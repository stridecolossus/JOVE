package org.sarge.jove.model.obj;

import java.io.IOException;
import java.util.Scanner;

/**
 * Parser for a <tt>newmtl</tt> command.
 * @author Sarge
 */
public class NewMaterialParser implements Parser {
	@Override
	public void parse(Scanner scanner, ObjectModel model) throws IOException {
		final String name = scanner.next();
		model.newMaterial(name);
	}
}
