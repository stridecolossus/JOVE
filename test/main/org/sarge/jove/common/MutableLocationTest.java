package org.sarge.jove.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MutableLocationTest {
	private MutableLocation loc;

	@Before
	public void before() {
		loc = new MutableLocation( 1, 2 );
	}

	@Test
	public void constructor() {
		assertEquals( 1, loc.getX() );
		assertEquals( 2, loc.getY() );
	}

	@Test
	public void set() {
		loc.set( 3, 4 );
		assertEquals( 3, loc.getX() );
		assertEquals( 4, loc.getY() );
	}

	@Test
	public void add() {
		loc.add( 3, 4 );
		assertEquals( 4, loc.getX() );
		assertEquals( 6, loc.getY() );
	}

	@Test
	public void equals() {
		assertTrue( loc.equals( loc ) );
		assertTrue( loc.equals( new Location( 1, 2 ) ) );
		assertFalse( loc.equals( null ) );
		assertFalse( loc.equals( new Location( 3, 4 ) ) );
	}
}
