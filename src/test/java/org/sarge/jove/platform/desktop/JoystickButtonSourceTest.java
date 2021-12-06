package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.desktop.DesktopButton.Action;

public class JoystickButtonSourceTest extends AbstractJoystickTest {
	private JoystickButtonSource src;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		src = new JoystickButtonSource(1, lib);
		handler = mock(Consumer.class);
	}

	@Test
	void constructor() {
		assertEquals(List.of(new DesktopButton("Button-0", Action.RELEASE)), src.buttons());
	}

	@Test
	void poll() {
		pressed = true;
		src.bind(handler);
		src.poll();
		verify(handler).accept(new DesktopButton("Button-0", Action.PRESS));
		src.poll();
		verifyNoMoreInteractions(handler);
	}
}
