package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;

public class TupleTest {
	private class MockTuple extends Tuple {
		public MockTuple( float x, float y, float z ) {
			super( x, y, z );
		}
	}

	private Tuple tuple;

	@Before
	public void before() {
		tuple = new MockTuple( 1, 2, 3 );
	}

	@Test
	public void constructor() {
		assertFloatEquals( 1, tuple.getX() );
		assertFloatEquals( 2, tuple.getY() );
		assertFloatEquals( 3, tuple.getZ() );
	}

	@Test
	public void dot() {
		tuple = new MockTuple( 0, 1, 0 );
		assertFloatEquals( 0, tuple.dot( new MockTuple( 0, 0, 0 ) ) );
		assertFloatEquals( 1, tuple.dot( new MockTuple( 1, 1, 0 ) ) );
		assertFloatEquals( 0, tuple.dot( new MockTuple( 1, 0, 0 ) ) );
		assertFloatEquals( -1, tuple.dot( new MockTuple( 0, -1, 0 ) ) );
	}

	@Test
	public void toArray() {
		final float[] array = new float[ 3 ];
		tuple.toArray( array );
		assertFloatEquals( 1, array[ 0 ] );
		assertFloatEquals( 2, array[ 1 ] );
		assertFloatEquals( 3, array[ 2 ] );
	}

	@Test
	public void append() {
		final FloatBuffer fb = BufferFactory.createFloatBuffer( 3 );
		tuple.append( fb );
		fb.flip();
		assertFloatEquals( 1, fb.get() );
		assertFloatEquals( 2, fb.get() );
		assertFloatEquals( 3, fb.get() );
	}

	@Test
	public void equals() {
		assertEquals( false, tuple.equals( null ) );
		final Tuple other = new MockTuple( 1, 2, 3 );
		assertEquals( true, tuple.equals( other ) );
		assertEquals( true, other.equals( tuple ) );
	}
}
