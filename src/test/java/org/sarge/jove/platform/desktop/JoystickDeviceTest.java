package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.platform.desktop.DesktopButton.Action;
import org.sarge.jove.util.MathsUtil;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JoystickDeviceTest {
	private JoystickDevice dev;
	private DesktopLibrary lib;
	private Consumer<Event> handler;
	private float pos;

	@BeforeEach
	void before() {
		// Init GLFW library
		lib = mock(DesktopLibrary.class);

		// Mock array count
		final IntByReference ref = new IntByReference(1) {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};

		// Init axis values array
		final Answer<Pointer> axes = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(Float.BYTES);
			ptr.setFloat(0, pos);
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickAxes(1, ref)).then(axes);

		// Init buttons array
		final Answer<Pointer> buttons = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) 1);
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickButtons(1, ref)).then(buttons);

		// Create device
		handler = mock(Consumer.class);
		dev = new JoystickDevice(1, "name", lib);
	}

	@Test
	void constructor() {
		assertEquals("name", dev.name());
		assertNotNull(dev.sources());
		assertEquals(2, dev.sources().size());
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

	@Test
	void buttons() {
		// Retrieve buttons
		final List<Button> buttons = dev.buttons();
		assertNotNull(buttons);
		assertEquals(1, buttons.size());

		// Check button
		final Button button = buttons.get(0);
		assertNotNull(button);

		// Check initialised to default
		assertEquals(Action.RELEASE, button.action());

		// Check buttons event source
		final Source<Button> src = dev.buttonSource();
		assertNotNull(src);
		assertEquals(true, dev.sources().contains(src));
	}

	@Test
	void poll() {
		dev.poll();
		verifyNoMoreInteractions(handler);
	}

	@Test
	void pollAxisEvent() {
		final Axis axis = dev.axes().get(0);
		pos = MathsUtil.HALF;
		axis.bind(handler);
		dev.poll();
		verify(handler).accept(new AxisEvent(axis, MathsUtil.HALF));
	}

	@Test
	void pollButtonEvents() {
		dev.buttonSource().bind(handler);
		dev.poll();
		verify(handler).accept(new DesktopButton("Button-0", Action.PRESS));
	}

	@Test
	void equals() {
		assertEquals(true, dev.equals(dev));
		assertEquals(false, dev.equals(null));
		assertEquals(false, dev.equals(mock(JoystickDevice.class)));
	}
}
