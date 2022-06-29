package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.*;

public class JoystickDeviceTest extends AbstractJoystickTest {
	private JoystickDevice dev;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		handler = mock(Consumer.class);
		dev = new JoystickDevice(1, "name", desktop);
	}

	@Test
	void constructor() {
		assertEquals("name", dev.name());
		assertNotNull(dev.sources());
		assertEquals(2, dev.sources().size());
	}

	@Test
	void buttonSource() {
		final JoystickButtonSource buttons = dev.buttons();
		assertNotNull(buttons);
		assertEquals(true, dev.sources().contains(buttons));
	}

	@Test
	void axes() {
		// Retrieve axes
		final List<Axis> axes = dev.axes();
		assertNotNull(axes);
		assertEquals(1, axes.size());

		// Check axis
		final Axis axis = axes.get(0);
		assertNotNull(axis);

		// Check initialised to current axis position
		assertEquals(0, axis.value());

		// Check sources
		assertEquals(true, dev.sources().contains(axis));
	}

//	@Test
//	void buttons() {
//		// Retrieve buttons
//		final List<Button> buttons = dev.buttons().buttons();
//		assertNotNull(buttons);
//		assertEquals(1, buttons.size());
//
//		// Check button
//		final Button button = buttons.get(0);
//		assertNotNull(button);
//
//		// Check initialised to default
//		assertEquals(Action.RELEASE, button.action());
//	}
//
//	@Test
//	void hats() {
//		// Retrieve hats
//		final List<Hat> hats = dev.buttons().hats();
//		assertNotNull(hats);
//		assertEquals(1, hats.size());
//
//		// Check button
//		final Hat hat = hats.get(0);
//		assertNotNull(hat);
//
//		// Check initialised to default
//		assertEquals(Set.of(HatAction.CENTERED), hat.action());
//	}

	@Test
	void poll() {
		dev.poll();
		verifyNoMoreInteractions(handler);
	}

	@Test
	void equals() {
		assertEquals(true, dev.equals(dev));
		assertEquals(false, dev.equals(null));
		assertEquals(false, dev.equals(mock(JoystickDevice.class)));
	}
}
