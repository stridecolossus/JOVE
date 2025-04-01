package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Event;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Callback;

class DesktopSourceTest {
	private DesktopSource<Callback, Event> source;
	private Window window;
	private BiConsumer<Window, Callback> method;
	private Callback listener;
	private Consumer<Event> handler;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void before() {
		// Create window
		final Desktop desktop = new Desktop(mock(DesktopLibrary.class), new ReferenceFactory());
		window = new Window(new Handle(1), desktop);

		// Init listener
		method = mock(BiConsumer.class);
		listener = mock(Callback.class);
		handler = mock(Consumer.class);

		// Create source
		source = new DesktopSource<>() {
			@Override
			public String name() {
				return null;
			}

			@Override
			public Window window() {
				return window;
			}

			@Override
			public Callback listener(Consumer<Event> handler) {
				return listener;
			}

			@Override
			public BiConsumer<Window, Callback> address(DesktopLibrary lib) {
				return method;
			}
		};
	}

	@Test
	void bind() {
		assertEquals(listener, source.bind(handler));
		verify(method).accept(window, listener);
	}
}
