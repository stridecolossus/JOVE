package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;

public class MutableLocationTest {
	private MutableLocation loc;
	
	@Before
	public void before() {
		loc = new MutableLocation( 1, 2 );
	}
	
	@Test
	public void constructor() {
		assertEquals( 1f, loc.getX(), 0.00001f );
		assertEquals( 2f, loc.getY(), 0.00001f );
	}
	
	@Test
	public void copyConstructor() {
		loc = new MutableLocation( new Location( 3, 4 ) );
		assertEquals( 3f, loc.getX(), 0.00001f );
		assertEquals( 4f, loc.getY(), 0.00001f );
	}
	
	@Test
	public void set() {
		loc.set( 3, 4 );
		assertEquals( 3f, loc.getX(), 0.00001f );
		assertEquals( 4f, loc.getY(), 0.00001f );
	}
	
	@Test
	public void add() {
		loc.add( 3, 4 );
		assertEquals( 4f, loc.getX(), 0.00001f );
		assertEquals( 6f, loc.getY(), 0.00001f );
	}
	
	@Test
	public void equals() {
		assertTrue( loc.equals( loc ) );
		assertTrue( loc.equals( new Location( 1, 2 ) ) );
		assertFalse( loc.equals( null ) );
		assertFalse( loc.equals( new Location( 3, 4 ) ) );
	}
}
