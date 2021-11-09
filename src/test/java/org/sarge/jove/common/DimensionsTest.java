package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DimensionsTest {
	private Dimensions dim;

	@BeforeEach
	void before() {
		dim = new Dimensions(640, 480);
	}

	@Test
	void constructor() {
		assertEquals(640, dim.width());
		assertEquals(480, dim.height());
	}

	@Test
	void area() {
		assertEquals(640 * 480, dim.area());
	}

	@Test
	void ratio() {
		assertEquals(640 / 480f, dim.ratio());
	}

	@Test
	void isSquare() {
		assertEquals(false, dim.isSquare());
		assertEquals(true, new Dimensions(2, 2).isSquare());
	}

	@Test
	void isLargerThan() {
		assertEquals(false, dim.isLargerThan(dim));
		assertEquals(true, dim.isLargerThan(new Dimensions(0, 0)));
		assertEquals(false, dim.isLargerThan(new Dimensions(999, 999)));
	}
}
