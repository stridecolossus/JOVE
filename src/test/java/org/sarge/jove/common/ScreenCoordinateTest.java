package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScreenCoordinateTest {
	private ScreenCoordinate coords;

	@BeforeEach
	public void before() {
		coords = new ScreenCoordinate(1, 2);
	}

	@Test
	public void constructor() {
		assertEquals(1, coords.x());
		assertEquals(2, coords.y());
	}

	@Test
	public void equals() {
		assertEquals(true, coords.equals(coords));
		assertEquals(false, coords.equals(null));
		assertEquals(false, coords.equals(new ScreenCoordinate(3, 4)));
	}
}
