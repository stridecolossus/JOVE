package org.sarge.jove.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

public class DeviceTest {
	@SuppressWarnings("unchecked")
	@Test
	void enable() {
		// Create a device
		final Device dev = mock(Device.class);
		when(dev.types()).thenReturn(Set.of(Position.class));

		// Enable all events and check specific type is enabled
		final Consumer<InputEvent<?>> handler = mock(Consumer.class);
		Device.enable(dev, handler);
		verify(dev).enable(Position.class, handler);
	}
}
