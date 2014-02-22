package org.sarge.jove.terrain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MutableHeightMapTest {
	@Test
	public void defaultConstructor() {
		final HeightMap map = new MutableHeightMap( 3, 4 );
		assertEquals( 3, map.getWidth() );
		assertEquals( 4, map.getHeight() );
	}
	
	@Test
	public void arrayConstructor() {
		final float[][] array = new float[ 3 ][ 4 ];
		final HeightMap map = new MutableHeightMap( array );
		assertEquals( 3, map.getWidth() );
		assertEquals( 4, map.getHeight() );
	}
	
	@Test
	public void setHeight() {
		final MutableHeightMap map = new MutableHeightMap( 3, 4 );
		map.setHeight( 2, 3, 42 );
		assertEquals( 42, map.getHeight( 2, 3 ), 0.0001f );
	}
}
