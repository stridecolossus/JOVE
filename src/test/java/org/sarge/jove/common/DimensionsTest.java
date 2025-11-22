package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

class DimensionsTest {
	private Dimensions dimensions;

	@BeforeEach
	void before() {
		dimensions = new Dimensions(640, 480);
	}

	@DisplayName("Dimensions have a width and height")
	@Test
	void constructor() {
		assertEquals(640, dimensions.width());
		assertEquals(480, dimensions.height());
	}

	@DisplayName("Dimensions have an area")
	@Test
	void area() {
		assertEquals(640 * 480, dimensions.area());
	}

	@DisplayName("Dimensions have an aspect ratio")
	@Test
	void ratio() {
		assertEquals(640 / 480f, dimensions.ratio());
	}

	@DisplayName("Dimensions can be square")
	@Test
	void isSquare() {
		assertEquals(false, dimensions.isSquare());
		assertEquals(true, new Dimensions(2, 2).isSquare());
	}

	@DisplayName("Dimensions can compared")
	@Test
	void contains() {
		assertEquals(true, dimensions.contains(dimensions));
		assertEquals(true, dimensions.contains(new Dimensions(1, 2)));
		assertEquals(false, dimensions.contains(new Dimensions(1024, 768)));
	}
}
