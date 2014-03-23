package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrimitiveTest {
	@Test
	public void getFaceCount() {
		final int num = 6;
		assertEquals( 6, Primitive.POINTS.getFaceCount( num ) );
		assertEquals( 3, Primitive.LINES.getFaceCount( num ) );
		assertEquals( 5, Primitive.LINE_STRIP.getFaceCount( num ) );
		assertEquals( 2, Primitive.TRIANGLES.getFaceCount( num ) );
		assertEquals( 4, Primitive.TRIANGLE_STRIP.getFaceCount( num ) );
		assertEquals( 4, Primitive.TRIANGLE_FAN.getFaceCount( num ) );
	}

	@Test
	public void isValidVertexCount() {
		// Check lines
		assertEquals( true, Primitive.LINES.isValidVertexCount( 2 ) );
		assertEquals( false, Primitive.LINES.isValidVertexCount( 3 ) );

		// Check triangles
		assertEquals( true, Primitive.TRIANGLES.isValidVertexCount( 3 ) );
		assertEquals( false, Primitive.TRIANGLES.isValidVertexCount( 4 ) );
	}
}
