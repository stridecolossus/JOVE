package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class FloatTableTest {
	private FloatTable table;

	@BeforeEach
	void before() {
		table = new FloatTable(new float[]{1, 2}, 1, 2);
	}

	@Disabled
	@Test
	void lookup() {
		//assertEquals(1, table.lookup(0));
		assertEquals(1, table.lookup(1));
		assertEquals(2, table.lookup(2));
	}

	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new FloatTable(new float[0], 0, 1));
	}



	@Test
	void of() {
		final var cos = FloatTable.of(8, 0, MathsUtil.TWO_PI, MathsUtil::cos);
		assertNotNull(cos);

//		System.out.println(MathsUtil.cos(0));
//		System.out.println(MathsUtil.cos(MathsUtil.HALF_PI));
//		System.out.println(MathsUtil.cos(MathsUtil.PI));
//		System.out.println(MathsUtil.cos(MathsUtil.PI + MathsUtil.HALF_PI));
//		System.out.println(MathsUtil.cos(MathsUtil.TWO_PI));


//		assertEquals(1, cos.lookup(0));
//		assertEquals(0, cos.lookup(MathsUtil.HALF_PI));
//		assertEquals(-1, cos.lookup(MathsUtil.PI));
		assertEquals(1, cos.lookup(MathsUtil.TWO_PI));
	}
}
