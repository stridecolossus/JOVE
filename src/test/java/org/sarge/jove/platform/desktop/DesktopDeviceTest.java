package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.platform.desktop.DesktopDevice.Action;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;
import org.sarge.jove.platform.desktop.DesktopDevice.Modifier;

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
	class ActionTests {
		@Test
		void map() {
			assertEquals("RELEASE", Action.map(0));
			assertEquals("PRESS", Action.map(1));
			assertEquals("REPEAT", Action.map(2));
		}
	}

	@Nested
	class ModifierTests {
		@Test
		void map() {
			assertEquals("SHIFT", Modifier.map(0x0001));
			assertEquals("CONTROL", Modifier.map(0x0002));
			assertEquals("ALT", Modifier.map(0x0004));
			assertEquals("SUPER", Modifier.map(0x0008));
			assertEquals("CAPS_LOCK", Modifier.map(0x0010));
			assertEquals("NUM_LOCK", Modifier.map(0x0020));
		}

		@Test
		void mask() {
			assertEquals("SHIFT-CONTROL-ALT", Modifier.map(0x0001 | 0x0002 | 0x0004));
		}
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
				public Collection<Type> types() {
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
		void name() {
			assertEquals("name-PRESS-SHIFT", DesktopDevice.name("name", 1, 0x0001));
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
}
