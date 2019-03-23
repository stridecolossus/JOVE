package org.sarge.jove.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.Device.Entry;

public class DeviceTest {
	private static final Object WINDOW = new Object();
	private static final Object CALLBACK = new Object();

	private Device<Object> device;
	private Function<Event.Handler, Object> factory;
	private BiConsumer<Object, Object> binder;
	private Event.Handler handler;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		// Create handler
		handler = mock(Event.Handler.class);

		// Create device entry
		factory = mock(Function.class);
		binder = mock(BiConsumer.class);
		when(factory.apply(handler)).thenReturn(CALLBACK);

		// Create device
		device = new Device<>(WINDOW, Map.of(Event.Category.BUTTON, new Entry<>(factory, binder)));
	}

	@Test
	public void constructor() {
		assertNotNull(device.active());
		assertEquals(true, device.active().isEmpty());
	}

	@Test
	public void categories() {
		assertEquals(Set.of(Event.Category.BUTTON), device.categories());
	}

	@Test
	public void bind() {
		device.bind(Event.Category.BUTTON, handler);
		verify(binder).accept(WINDOW, CALLBACK);
		assertEquals(Set.of(Event.Category.BUTTON), device.active());
	}

	@Test
	public void bindReplaceExisting() {
		device.bind(Event.Category.BUTTON, handler);
		device.bind(Event.Category.BUTTON, handler);
		verify(binder, times(2)).accept(WINDOW, CALLBACK);
	}

	@Test
	public void bindInvalidCategory() {
		assertThrows(UnsupportedOperationException.class, () -> device.bind(Event.Category.CLICK, handler));
	}

	@Test
	public void remove() {
		device.bind(Event.Category.BUTTON, handler);
		device.remove(Event.Category.BUTTON);
		verify(binder).accept(WINDOW, null);
		assertEquals(true, device.active().isEmpty());
	}

	@Test
	public void removeInvalidCategory() {
		assertThrows(IllegalStateException.class, () -> device.remove(Event.Category.CLICK));
	}

	@Test
	public void removeNotBound() {
		assertThrows(IllegalStateException.class, () -> device.remove(Event.Category.BUTTON));
	}

	@Test
	public void clear() {
		device.bind(Event.Category.BUTTON, handler);
		device.clear();
		verify(binder).accept(WINDOW, null);
		assertEquals(true, device.active().isEmpty());
	}
}
