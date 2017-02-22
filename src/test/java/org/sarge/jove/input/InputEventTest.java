package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class InputEventTest {
	private InputEvent event;
	private EventKey key;
	private Device dev;

	@Before
	public void before() {
		key = new EventKey(EventType.PRESS, "name");
		dev = mock(Device.class);
		event = new InputEvent(dev, key);
	}

	@Test
	public void constructor() {
		assertEquals(dev, event.getDevice());
		assertEquals(key, event.getEventKey());
		assertEquals(null, event.getLocation());
		assertEquals(null, event.getZoom());
	}

	@Test
	public void setLocation() {
		key.init(EventType.PRESS, "key");
		event.init(dev, key);
		final Location loc = new Location();
		event.setLocation(loc);
		assertEquals(loc, event.getLocation());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLocationInvalid() {
		key.init(EventType.ORIENTATE, "key");
		event.init(dev, key);
		event.setLocation(new Location());
	}

	@Test
	public void setZoom() {
		key.init(EventType.ZOOM, null);
		event.init(dev, key);
		final Integer zoom = 42;
		event.setZoom(zoom);
		assertEquals(zoom, event.getZoom());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setZoomInvalid() {
		key.init(EventType.ORIENTATE, null);
		event.init(dev, key);
		event.setZoom(42);
	}
}
