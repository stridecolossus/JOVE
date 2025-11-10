package org.sarge.jove.platform.obj;

import org.junit.jupiter.api.Test;

class GroupParserTest {
	@Test
	void parse() {
		final var model = new ObjectModel();
		final var group = new GroupParser(model);
		group.parse(null);
	}
}
