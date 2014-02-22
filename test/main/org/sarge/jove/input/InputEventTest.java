package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.sarge.jove.common.Location;

public class InputEventTest {
	@Test
	public void test() {
		final Device dev = mock( Device.class );
		final EventName key = new EventName( EventType.PRESS, "key" );
		final InputEvent event = new InputEvent( dev, key, new Location( 1, 2 ), 3 );
		assertEquals( dev, event.getDevice() );
		assertEquals( key, event.getEventKey() );
		assertNotNull( event.getLocation() );
		assertNotNull( event.getZoom() );
	}
}
