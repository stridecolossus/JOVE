package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.util.ReferenceFactory;

public class KeyboardDeviceTest {
	private KeyboardDevice dev;
	private Window window;

	@BeforeEach
	void before() {
		final Desktop desktop = new Desktop(mock(DesktopLibrary.class), new ReferenceFactory());
		window = new Window(new Handle(1), desktop);
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
