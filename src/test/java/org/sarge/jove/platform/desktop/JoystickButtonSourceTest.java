package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.desktop.DesktopButton.Action;
import org.sarge.jove.platform.desktop.JoystickButtonSource.Hat;
import org.sarge.jove.platform.desktop.JoystickButtonSource.HatAction;

public class JoystickButtonSourceTest extends AbstractJoystickTest {
	private JoystickButtonSource src;
	private Consumer<Event> handler;
	private Hat hat;

	@BeforeEach
	void before() {
		src = new JoystickButtonSource(1, lib);
		handler = mock(Consumer.class);
		hat = src.new Hat(0, (byte) 0);
	}

	@Test
	void constructor() {
		assertEquals(List.of(new DesktopButton("Button-0", Action.RELEASE)), src.buttons());
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
		verify(handler).accept(new DesktopButton("Button-0", Action.PRESS));
		verify(handler).accept(src.new Hat(0, (byte) (1 | 2)));

		// Check buttons updated to new state
		src.poll();
		verifyNoMoreInteractions(handler);
	}

	@Nested
	class HatTests {
		@Test
		void constructor() {
			assertEquals(Set.of(HatAction.CENTERED), hat.action());
			assertEquals(hat, hat.type());
			assertEquals("Hat-1", hat.name());
		}

		@Test
		void update() {
			src.bind(handler);
			hat.update((byte) (1 | 2));
			assertEquals(Set.of(HatAction.UP, HatAction.RIGHT), hat.action());
			assertEquals("Hat-1-[UP, RIGHT]", hat.name());
		}
	}
}
