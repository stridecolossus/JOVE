package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.desktop.DesktopLibraryJoystick.JoystickListener;
import org.sarge.jove.platform.desktop.JoystickDevice.ConnectionListener;
import org.sarge.jove.platform.desktop.JoystickDevice.JoystickAxis;
import org.sarge.jove.platform.desktop.JoystickDevice.Manager;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JoystickDeviceTest {
	private JoystickDevice dev;
	private DesktopLibrary lib;
	private JoystickAxis axis;
	private Button button;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		axis = new JoystickAxis(2, 3);
		button = new Button("button");
		dev = new JoystickDevice(1, "name", new JoystickAxis[]{axis}, new Button[]{button}, lib);
		handler = mock(Consumer.class);
	}

	@Test
	void constructor() {
		assertEquals("name", dev.name());
		assertEquals(List.of(axis), dev.axes());
		assertEquals(List.of(button), dev.buttons());
		assertNotNull(dev.buttonSource());
		assertEquals(Set.of(axis, dev.buttonSource()), dev.sources());
	}

	private void init() {
		when(lib.glfwGetJoystickAxes(eq(1), any(IntByReference.class))).thenReturn(new Pointer(0));
		when(lib.glfwGetJoystickButtons(eq(1), any(IntByReference.class))).thenReturn(new Pointer(0));
	}

	@Test
	void pollEmpty() {
		init();
		axis.bind(handler);
		dev.poll();
		verifyNoMoreInteractions(handler);
	}

	private void initAxisValues() {
		final Answer<Pointer> axes = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(Float.BYTES);
			ptr.setFloat(0, 4);
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickAxes(eq(1), any(IntByReference.class))).then(axes);
	}

	@Test
	void pollAxisEvent() {
		initAxisValues();
		axis.bind(handler);
		dev.poll();
		verify(handler).accept(new AxisEvent(axis, 4));
	}

	private void initButtons() {
		final Answer<Pointer> buttons = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) 1);
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickButtons(eq(1), any(IntByReference.class))).then(buttons);
	}

	@Test
	void pollButtonEvents() {
		init();
		initButtons();
		dev.buttonSource().bind(handler);
		dev.poll();
		verify(handler).accept(button);
	}

	@Test
	void equals() {
		assertEquals(true, dev.equals(dev));
		assertEquals(false, dev.equals(null));
		assertEquals(false, dev.equals(mock(JoystickDevice.class)));
	}

	@Nested
	class JoystickAxisTests {
		@Test
		void constructor() {
			assertEquals(3, axis.value());
		}

		@Test
		void bind() {
			axis.bind(handler);
		}

		@Test
		void update() {
			axis.update(4);
			assertEquals(4, axis.value());
		}
	}

	@Nested
	class ManagerTests {
		private Manager manager;

		@BeforeEach
		void before() {
			final Desktop desktop = mock(Desktop.class);
			when(desktop.library()).thenReturn(lib);
			when(lib.glfwJoystickPresent(1)).thenReturn(true);
			when(lib.glfwGetJoystickName(1)).thenReturn("name");
			initAxisValues();
			initButtons();
			manager = new Manager(desktop);
		}

		@Test
		void create() {
			for(int n = 0; n < 16; ++n) {
				verify(lib).glfwJoystickPresent(n);
			}
		}

		@Test
		void devices() {
			assertEquals(List.of(dev), manager.devices());
		}

		@Test
		void poll() {
			manager.poll();
		}

		@Test
		void connect() {
			// Register connection listener
			final var listener = mock(ConnectionListener.class);
			manager.listener(listener);

			// Check API
			final ArgumentCaptor<JoystickListener> captor = ArgumentCaptor.forClass(JoystickListener.class);
			verify(lib).glfwSetJoystickCallback(captor.capture());

			// Capture delegate listener
			final JoystickListener delegate = captor.getValue();
			assertNotNull(delegate);

			// Disconnect device
			delegate.connect(1, 0);
			assertEquals(List.of(), manager.devices());

			// Connect device
			delegate.connect(1, 0x00040001);
			assertEquals(List.of(dev), manager.devices());
		}
	}
}
