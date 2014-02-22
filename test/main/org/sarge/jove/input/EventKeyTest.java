package org.sarge.jove.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EventKeyTest {
	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void emptyEventName() {
		new EventName( EventType.PRESS, "" );
	}

	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void invalidEventName() {
		new EventName( EventType.PRESS, "contains spaces" );
	}

	@Test
	public void equals() {
		final EventName key = new EventName( EventType.PRESS, "name" );
		assertTrue( key.equals( key ) );
		assertTrue( key.equals( new EventName( EventType.PRESS, "name" ) ) );
		assertFalse( key.equals( null ) );
		assertFalse( key.equals( new EventName( EventType.RELEASE, "name" ) ) );
	}
}
