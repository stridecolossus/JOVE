package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.*;
import org.sarge.jove.util.FloatSupport.ArrayConverter;

public class FloatSupportTest {
	@Nested
	class ArrayConverterTests {
		private ArrayConverter<Coordinate> converter;

		@BeforeEach
		void before() {
			converter = new ArrayConverter<>(2, Coordinate::of);
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
		void minimum() {
			converter = new ArrayConverter<>(2, false, Coordinate::of);
			assertEquals(Coordinate2D.BOTTOM_LEFT, converter.apply("0, 1"));
			assertEquals(new Coordinate1D(1), converter.apply("1"));
		}

		@Test
		void invalid() {
			assertThrows(NumberFormatException.class, () -> converter.apply("1,doh"));
		}
	}
}
