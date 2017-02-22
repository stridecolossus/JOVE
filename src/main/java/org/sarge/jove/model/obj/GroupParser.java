package org.sarge.jove.model.obj;

import java.util.Scanner;

/**
 * Polygon group / object parser.
 * @author Sarge
 */
public class GroupParser implements Parser {
	@Override
	public void parse(Scanner scanner, ObjectModel model) {
		// Load optional group name
		final String name;
		if(scanner.hasNext()) {
			name = scanner.next();
		}
		else {
			name = null;
		}
		
		// Start new group
		model.newGroup(name);
	}
}
