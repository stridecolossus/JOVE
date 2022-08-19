package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.Action;

@Disabled
public class JoystickButtonSourceTest extends AbstractJoystickTest {
	private JoystickButtonSource src;
	private Consumer<Event> handler;
	private Button<Action> button;
//	private Hat hat;

	@BeforeEach
	void before() {
		src = new JoystickButtonSource(1, desktop);
		handler = mock(Consumer.class);
		button = new Button<>("Button-0", Action.RELEASE);
//		hat = new Hat("Hat-0");
	}

	@Test
	void constructor() {
		assertEquals(List.of(button), src.buttons());
//		assertEquals(List.of(hat), src.hats());
	}

//	@Test
//	void poll() {
//		// Attach handler
//		src.bind(handler);
//
//		// Poll for modified buttons
//		pressed = true;
//		src.poll();
//
//		// Check events generated
//		verify(handler).accept(button.resolve(1));
//		verify(handler).accept(hat.resolve((1 | 2)));
//
//		// Check buttons updated to new state
//		src.poll();
//		verifyNoMoreInteractions(handler);
//	}
}
