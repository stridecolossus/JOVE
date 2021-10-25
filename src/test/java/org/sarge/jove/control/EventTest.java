package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class EventTest {
	@Test
	void name() {
		assertEquals("one-two", Event.name("one", StringUtils.EMPTY, "two"));
	}
}
