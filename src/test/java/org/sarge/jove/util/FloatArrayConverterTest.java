package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;

class FloatArrayConverterTest {
	private FloatArrayConverter<Coordinate> converter;

	@BeforeEach
	void before() {
		converter = new FloatArrayConverter<>(2, Coordinate::of);
	}

	@Test
	void convert() {
		assertEquals(Coordinate2D.BOTTOM_LEFT, converter.apply("0, 1"));
		assertEquals(Coordinate2D.BOTTOM_LEFT, converter.apply("0 1"));
	}

	@Test
	void exact() {
		assertThrows(IllegalArgumentException.class, () -> converter.apply("1"));
		assertThrows(IllegalArgumentException.class, () -> converter.apply("0,1,2"));
	}

	@Test
	void invalid() {
		assertThrows(NumberFormatException.class, () -> converter.apply("1,doh"));
	}
}
