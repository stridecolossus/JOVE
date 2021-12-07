package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.DefaultButton;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Hat;

public class JoystickButtonSourceTest extends AbstractJoystickTest {
	private JoystickButtonSource src;
	private Consumer<Event> handler;
	private Hat hat;

	@BeforeEach
	void before() {
		src = new JoystickButtonSource(1, lib);
		handler = mock(Consumer.class);
		hat = new Hat(1, 0);
	}

	@Test
	void constructor() {
		assertEquals(List.of(new DefaultButton("Button-0").resolve(0, 0)), src.buttons());
		assertEquals(List.of(hat), src.hats());
	}

	@Test
	void poll() {
		// Attach handler
		src.bind(handler);

		// Poll for modified buttons
		pressed = true;
		src.poll();

		// Check events generated
		verify(handler).accept(new DefaultButton("Button-0"));
		verify(handler).accept(new Hat(1, (byte) (1 | 2)));

		// Check buttons updated to new state
		src.poll();
		verifyNoMoreInteractions(handler);
	}
}
