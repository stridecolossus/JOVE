package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
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
			public Set<Source> sources() {
				return null;
			}

			@Override
			public void close() {
			}
		};
	}

	@Nested
	class DesktopSourceTests {
		private DesktopLibrary lib;
		private DesktopSource<Object> src;
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
				public List<? extends Type<?>> types() {
					return null;
				}

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
		void constructor() {
			assertEquals(dev, src.device());
		}

		@Test
		void enable() {
			src.bind(handler);
			verify(method).accept(window, listener);
			verify(window).register(handler, listener);
		}

		@Test
		void disable() {
			src.disable();
			verify(method).accept(window, null);
		}
	}

	@Nested
	class ActionTests {
		@Test
		void map() {
			assertEquals(Action.RELEASE, DesktopDevice.map(0));
			assertEquals(Action.PRESS, DesktopDevice.map(1));
			assertEquals(Action.REPEAT, DesktopDevice.map(2));
		}

		@Test
		void unknown() {
			assertThrows(RuntimeException.class, () -> DesktopDevice.map(999));
		}
	}
}
