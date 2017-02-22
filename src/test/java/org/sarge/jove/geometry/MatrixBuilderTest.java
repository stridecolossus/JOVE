package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class MatrixBuilderTest {
	private MatrixBuilder builder;
	private Vector vec;

	@Before
	public void before() {
		builder = new MatrixBuilder(4);
		vec = new Vector(1, 2, 3);
	}

	@Test
	public void constructor() {
		assertEquals(new Matrix(4), builder);
	}
	
	@Test
	public void identity() {
		final Matrix identity = builder.identity().build();
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				if(r == c) {
					assertFloatEquals(1, identity.get(r, c));
				}
				else {
					assertFloatEquals(0, identity.get(r, c));
				}
			}
		}
	}

	@Test
	public void set() {
		builder.set(2, 3, 42);
		final Matrix matrix = builder.build();
		assertFloatEquals(42, matrix.get(2, 3));
	}

	@Test
	public void setRow() {
		builder.setRow(1, vec);
		final Matrix matrix = builder.build();
		assertFloatEquals(1, matrix.get(1, 0));
		assertFloatEquals(2, matrix.get(1, 1));
		assertFloatEquals(3, matrix.get(1, 2));
	}

	@Test
	public void setColumn() {
		builder.setColumn(1, vec);
		final Matrix matrix = builder.build();
		assertFloatEquals(1, matrix.get(0, 1));
		assertFloatEquals(2, matrix.get(1, 1));
		assertFloatEquals(3, matrix.get(2, 1));
	}
}
