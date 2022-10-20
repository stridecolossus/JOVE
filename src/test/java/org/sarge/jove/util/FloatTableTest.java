package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;

@Disabled
public class FloatTableTest {
	private FloatTable.Builder builder;

	@BeforeEach
	void before() {
		builder = new FloatTable.Builder();
	}

	@Test
	void builder() {
		final FloatTable table = builder
				.start(0)
				.end(256)
				.build(FloatUnaryOperator.IDENTITY);

		for(int n = 0; n < 256; ++n) {
			assertEquals(n, table.apply(n));
		}
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.size(0));
	}

	@Test
	void ascending() {
		builder.start(2);
		builder.end(1);
		assertThrows(IllegalArgumentException.class, () -> builder.build(FloatUnaryOperator.IDENTITY));
	}

	@Nested
	class CosineTest {
		private FloatTable cos;

		@BeforeEach
		void before() {
			cos = FloatTable.cos();
		}

		@Test
		void cos() {
			// Explicitly populate cardinal entries
			// TODO

			// Validate table
			for(int n = 0; n < 360; ++n) {
				final float angle = MathsUtil.toRadians(n);
				final float expected = MathsUtil.cos(angle);
System.out.println(n+" "+expected+" "+cos.apply(angle));
				assertTrue(Math.abs(expected - cos.apply(angle)) < 0.005f); // TODO - accuracy
			}

			// TODO - cardinals, 360, 4 x PI
		}

		@Test
		void cardinal() {
			assertEquals(1, cos.apply(0));
			assertEquals(0, cos.apply(MathsUtil.HALF_PI));
			assertEquals(0, cos.apply(MathsUtil.PI));
			assertEquals(-1, cos.apply(MathsUtil.PI + MathsUtil.HALF_PI));
			assertEquals(0, cos.apply(MathsUtil.TWO_PI));
		}
	}
}


////table[0] = 0f;
////table[(int)(90 * degToIndex) & SIN_MASK] = 1f;
////table[(int)(180 * degToIndex) & SIN_MASK] = 0f;
////table[(int)(270 * degToIndex) & SIN_MASK] = -1f;


//
//	//@Test
//	void cos() {
//		final FloatTable table = FloatTable.of(0, MathsUtil.TWO_PI, 4096, MathsUtil::cos);
//
//		for(int a = 0; a <= 360; ++a) {
//			final float rad = MathsUtil.toRadians(a);
//			final float cos = MathsUtil.cos(rad);
//			System.out.println(a+" "+cos+" "+table.get(rad));
////			assertTrue(MathsUtil.isEqual(cos, table.get(rad)));
//			assertTrue(cos - table.get(rad) < 0.001f); // TODO
//		}
//	}
//
//	private static final Random random = new Random();
//
//	@Test
//	void base() {
//		final long start = System.currentTimeMillis();
//		for(long n = 0; n < 1000000000L; ++n) {
//			final float a = random.nextFloat() * 1000;
//			Math.sqrt(a);
//		}
//		System.out.println("base="+(System.currentTimeMillis() - start));
//	}
//
//	@Test
//	void lookup() {
//		final FloatTable table = FloatTable.of(0, 1000, 4096, MathsUtil::sqrt);
//		final long start = System.currentTimeMillis();
//		for(long n = 0; n < 1000000000L; ++n) {
//			final float a = random.nextFloat() * 1000;
//			table.get(a);
//		}
//		System.out.println("lookup="+(System.currentTimeMillis() - start));
//	}
//}
//
////	//@Test
////	void get() {
////		final FloatTable table = FloatTable.inclusive(0, 20, 4, FloatUnaryOperator.IDENTITY);
////		for(int n = 0; n < 20; ++n) {
////			assertEquals(n / 4 * 4, table.get(n));
////		}
////	}
////
////	//@Test
////	void cos() {
////		final FloatTable table = FloatTable.inclusive(0, MathsUtil.TWO_PI, MathsUtil.toRadians(45), MathsUtil::cos);
////		for(int a = 0; a < 360; ++a) {
////			final float quantised = MathsUtil.toRadians(a / 45 * 45);
////			final float expected = MathsUtil.cos(quantised);
////System.out.println(a+" "+expected+" "+table.get(MathsUtil.toRadians(a)));
//////			assertTrue(MathsUtil.isEqual(expected, table.get(MathsUtil.toRadians(a))));
////		}
////	}
////}
