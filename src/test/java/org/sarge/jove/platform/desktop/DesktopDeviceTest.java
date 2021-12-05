package org.sarge.jove.platform.desktop;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;

public class DesktopDeviceTest {
	private DesktopDevice dev;
	private Window window;

	@BeforeEach
	void before() {
		// Create window
		window = mock(Window.class);
		when(window.handle()).thenReturn(new Handle(1));

		// Create device
		dev = new DesktopDevice(window) {
			@Override
			public Set<Source<?>> sources() {
				return null;
			}
		};
	}

	@Nested
	class DesktopSourceTests {
		private DesktopLibrary lib;
		private DesktopSource<Object, Event> src;
		private BiConsumer<Window, Object> method;
		private Object listener;
		private Consumer<Event> handler;

		@BeforeEach
		void before() {
			// Init GLFW
			lib = mock(DesktopLibrary.class);

			// Init dependencies
			method = mock(BiConsumer.class);
			listener = new Object();
			handler = mock(Consumer.class);

			// Init window
			final Desktop desktop = mock(Desktop.class);
			when(window.desktop()).thenReturn(desktop);
			when(desktop.library()).thenReturn(lib);

			// Create source
			src = dev.new DesktopSource<>() {
				@Override
				protected BiConsumer<Window, Object> method(DesktopLibrary lib) {
					return method;
				}

				@Override
				protected Object listener(Consumer<Event> handler) {
					return listener;
				}
			};
		}

		@Test
		void bind() {
			src.bind(handler);
			verify(method).accept(window, listener);
		}

		@Test
		void disable() {
			src.bind(null);
			verify(method).accept(window, null);
		}
	}
}
