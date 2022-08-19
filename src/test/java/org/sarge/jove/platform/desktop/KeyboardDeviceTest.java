package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

public class KeyboardDeviceTest {
	private KeyboardDevice dev;
	private Window window;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		// Init desktop
		final Desktop desktop = mock(Desktop.class);
		lib = mock(DesktopLibrary.class);
		when(desktop.library()).thenReturn(lib);

		// Create window
		window = mock(Window.class);
		when(window.desktop()).thenReturn(desktop);

		// Create device
		dev = new KeyboardDevice(window);
	}

	@Test
	void sources() {
		assertNotNull(dev.keyboard());
		assertEquals(Set.of(dev.keyboard()), dev.sources());
	}

	@SuppressWarnings("unchecked")
	@Test
	void bind() {
		final Consumer<Button<Action>> handler = mock(Consumer.class);
		final Source<Button<Action>> source = dev.keyboard();
		final KeyListener listener = (KeyListener) source.bind(handler);
		assertNotNull(listener);
		listener.key(null, 256, 0, 1, 2);
		verify(handler).accept(new Button<>("ESCAPE", Action.PRESS));
		// TODO - modifiers
	}
}
