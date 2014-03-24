package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Test;

public class ObjectModelHelperTest {
	@Test
	public void toArray() {
		final float[] array = new float[ 2 ];
		ObjectModelHelper.toArray( new String[]{ "0.1", "0.2" }, array );
		assertFloatEquals( 0.1f, array[ 0 ] );
		assertFloatEquals( 0.2f, array[ 1 ] );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toArrayInvalidLength() {
		final float[] array = new float[ 42 ];
		ObjectModelHelper.toArray( new String[]{ "0.1", "0.2" }, array );
	}

	@Test
	public void toSingleString() {
		final String str = "string";
		final String result = ObjectModelHelper.toString( new String[]{ str }, null );
		assertEquals( str, result );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toSingleStringInvalidLength() {
		ObjectModelHelper.toString( new String[]{ "one", "two" }, "error" );
	}
}
