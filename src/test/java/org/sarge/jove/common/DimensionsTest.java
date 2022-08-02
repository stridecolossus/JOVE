package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

class DimensionsTest {
	private Dimensions dim;

	@BeforeEach
	void before() {
		dim = new Dimensions(640, 480);
	}

	@DisplayName("A 2D dimension has width and height")
	@Test
	void constructor() {
		assertEquals(640, dim.width());
		assertEquals(480, dim.height());
	}

	@DisplayName("The area of a 2D dimension can be calculated")
	@Test
	void area() {
		assertEquals(640 * 480, dim.area());
	}

	@DisplayName("A 2D dimension can be represented by its aspect ratio")
	@Test
	void ratio() {
		assertEquals(640 / 480f, dim.ratio());
	}

	@DisplayName("A 2D dimension can be square")
	@Test
	void isSquare() {
		assertEquals(false, dim.isSquare());
		assertEquals(true, new Dimensions(2, 2).isSquare());
	}

	@DisplayName("A 2D dimension can be compared to another instance")
	@Test
	void compareTo() {
		assertEquals(0, dim.compareTo(dim));
		assertEquals(1, dim.compareTo(new Dimensions(0, 0)));
		assertEquals(1, dim.compareTo(new Dimensions(640, 0)));
		assertEquals(-1, dim.compareTo(new Dimensions(999, 999)));
	}

	@DisplayName("A 2D dimension can be converted to a rectangle at the origin")
	@Test
	void rectangle() {
		assertEquals(new Rectangle(0, 0, 640, 480), dim.rectangle());
	}
}
