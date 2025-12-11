package org.sarge.jove.platform.obj;

import org.junit.jupiter.api.Test;

class GroupParserTest {
	@Test
	void parse() {
		final var group = new GroupParser(new ObjectModel());
		group.parse(null);
	}
}
