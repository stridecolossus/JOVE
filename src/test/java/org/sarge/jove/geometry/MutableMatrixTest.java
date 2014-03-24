package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class MutableMatrixTest {
	private MutableMatrix matrix;
	private Vector vec;

	@Before
	public void before() {
		matrix = new MutableMatrix( 4 );
		vec = new Vector( 1, 2, 3 );
	}

	@Test
	public void constructor() {
		assertEquals( new Matrix( 4 ), matrix );
	}

	@Test
	public void set() {
		matrix.set( 2, 3, 42 );
		assertFloatEquals( 42, matrix.get( 2, 3 ) );
	}

	@Test
	public void setRow() {
		matrix.setRow( 1, vec );
		assertEquals( vec, matrix.getRow( 1 ) );
		assertFloatEquals( 1, matrix.get( 1, 0 ) );
		assertFloatEquals( 2, matrix.get( 1, 1 ) );
		assertFloatEquals( 3, matrix.get( 1, 2 ) );
	}

	@Test
	public void setColumn() {
		matrix.setColumn( 1, vec );
		assertEquals( vec, matrix.getColumn( 1 ) );
		assertFloatEquals( 1, matrix.get( 0, 1 ) );
		assertFloatEquals( 2, matrix.get( 1, 1 ) );
		assertFloatEquals( 3, matrix.get( 2, 1 ) );
	}
}
