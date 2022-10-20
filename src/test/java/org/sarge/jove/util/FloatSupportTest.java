package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.FloatSupport.*;

public class FloatSupportTest {
	@Nested
	class UnaryOperatorTests {
		private FloatUnaryOperator op;

		@BeforeEach
		void before() {
			op = f -> f + 1;
		}

		@Test
		void identity() {
			assertEquals(3, FloatUnaryOperator.IDENTITY.apply(3));
		}

		@Test
		void compose() {
			assertEquals(2, FloatUnaryOperator.IDENTITY.compose(op).apply(1));
		}

		@Test
		void then() {
			assertEquals(2, FloatUnaryOperator.IDENTITY.then(op).apply(1));
		}
	}

	@Nested
	class GeneratorTests {
		@Test
		void setAll() {
			final float[] array = new float[2];
			IntToFloatFunction.setAll(array, f -> f);
			assertEquals(0f, array[0]);
			assertEquals(1f, array[1]);
		}
	}
}
