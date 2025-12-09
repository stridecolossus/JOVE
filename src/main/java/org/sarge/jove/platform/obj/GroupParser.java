package org.sarge.jove.platform.obj;

import static java.util.Objects.requireNonNull;

import java.util.Scanner;

/**
 * The <i>group</i> parser starts a new object or smoothing group of the model.
 * @see ObjectModel#start()
 * @author Sarge
 */
class GroupParser implements Parser {
	private final ObjectModel model;

	/**
	 * Constructor.
	 * @param model OBJ model
	 */
	public GroupParser(ObjectModel model) {
		this.model = requireNonNull(model);
	}

	@Override
	public void parse(Scanner scanner) {
		model.start();
	}
}
