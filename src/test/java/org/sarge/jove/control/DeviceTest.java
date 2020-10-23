package org.sarge.jove.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class DeviceTest {
	@Test
	void enable() {
		final InputEvent.Handler handler = mock(InputEvent.Handler.class);
		final Device dev = mock(Device.class);
		when(dev.types()).thenReturn(Set.of(InputEvent.Type.Position.class));
		Device.enable(dev, handler);
		verify(dev).enable(InputEvent.Type.Position.class, handler);
	}
}
