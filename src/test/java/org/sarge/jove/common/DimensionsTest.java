package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DimensionsTest {
	private Dimensions dim;

	@BeforeEach
	public void before() {
		dim = new Dimensions(640, 480);
	}

	@Test
	public void constructor() {
		assertEquals(640, dim.width());
		assertEquals(480, dim.height());
		assertEquals(640 / 480f, dim.ratio());
	}

	@Test
	public void isSquare() {
		assertEquals(false, dim.isSquare());
		assertEquals(true, new Dimensions(2, 2).isSquare());
	}

	@Test
	public void isPowerOfTwo() {
		assertEquals(true, new Dimensions(2, 2).isPowerOfTwo());
		assertEquals(false, new Dimensions(0, 0).isPowerOfTwo());
		assertEquals(false, new Dimensions(3, 4).isPowerOfTwo());
	}

	@Test
	public void exceeds() {
		assertEquals(false, dim.exceeds(dim));
		assertEquals(true, dim.exceeds(new Dimensions(0, 0)));
		assertEquals(false, dim.exceeds(new Dimensions(999, 999)));
	}
}
