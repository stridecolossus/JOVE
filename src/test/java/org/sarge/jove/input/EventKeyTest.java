package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class EventKeyTest {
	private EventKey key;

	@Before
	public void before() {
		key = new EventKey(EventType.PRESS, "name");
	}
	
	@Test
	public void constructor() {
		assertEquals(EventType.PRESS, key.getType());
		assertEquals("name", key.getName());
	}
	
	@Test
	public void equals() {
		assertEquals(true, key.equals(new EventKey(EventType.PRESS, "name")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyEventName() {
		new EventKey(EventType.PRESS, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidEventName() {
		new EventKey(EventType.PRESS, "contains spaces");
	}

	@Test(expected = IllegalArgumentException.class)
	public void superfluousEventName() {
		new EventKey(EventType.ZOOM, "whatever");
	}
}
