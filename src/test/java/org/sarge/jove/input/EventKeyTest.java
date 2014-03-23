package org.sarge.jove.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EventKeyTest {
	private EventKey key;

	@Before
	public void before() {
		key = EventKey.POOL.get();
	}

	@Test(expected=IllegalArgumentException.class)
	public void emptyEventName() {
		key.init( EventType.PRESS, "" );
	}

	@Test(expected=IllegalArgumentException.class)
	public void invalidEventName() {
		key.init( EventType.PRESS, "contains spaces" );
	}

	@Test
	public void equals() {
		key.init( EventType.PRESS, "name" );
		assertTrue( key.equals( key ) );
		assertTrue( key.equals( EventKey.POOL.get().init( EventType.PRESS, "name" ) ) );
		assertFalse( key.equals( null ) );
		assertFalse( key.equals( EventKey.POOL.get().init( EventType.RELEASE, "name" ) ) );
	}
}
