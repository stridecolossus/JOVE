package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.DesktopDisplayMode;
import org.sarge.jove.platform.desktop.Monitor.DisplayMode;
import org.sarge.jove.util.MockReferenceFactory;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

class MonitorTest {
	private Monitor monitor;
	private DisplayMode mode;
	private DesktopDisplayMode struct;
	private Desktop desktop;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		// Init desktop service
		lib = mock(DesktopLibrary.class);
		desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);

		// Init GLFW display mode
		struct = new DesktopDisplayMode();
		struct.width = 640;
		struct.height = 480;
		struct.red = 1;
		struct.green = 2;
		struct.blue = 3;
		struct.refresh = 60;

		// Create monitor
		mode = new DisplayMode(new Dimensions(640, 480), List.of(1, 2, 3), 60);
		monitor = new Monitor(new Handle(1), "name", new Dimensions(2, 3), List.of(mode));
	}

	@Test
	void constructor() {
		assertNotNull(monitor.handle());
		assertEquals("name", monitor.name());
		assertEquals(new Dimensions(2, 3), monitor.size());
		assertEquals(List.of(mode), monitor.modes());
	}

	@Test
	void mode() {
		assertEquals(new Dimensions(640, 480), mode.size());
		assertEquals(List.of(1, 2, 3), mode.depth());
		assertEquals(60, mode.refresh());
	}

	@Test
	void current() {
		when(lib.glfwGetVideoMode(monitor)).thenReturn(struct);
		assertEquals(mode, monitor.mode(desktop));
	}

	@Test
	void monitors() {
//		// Init integer-by-reference return values
//		final IntByReference count = integer(1);
//		final var integers = List.of(count, integer(2), integer(3), count);
//
//		final ReferenceFactory factory = mock(ReferenceFactory.class);
//		when(factory.integer()).then(AdditionalAnswers.returnsElementsOf(integers));
//		when(desktop.factory()).thenReturn(factory);

		// Init reference factory
		final var factory = new MockReferenceFactory();
		final IntByReference count = factory.integer();
		when(desktop.factory()).thenReturn(factory);

		// Init array of monitors
		final Pointer handle = new Pointer(1);
		final Pointer array = new Memory(8);
		array.setPointer(0, handle);
		when(lib.glfwGetMonitors(count)).thenReturn(array);

		// Init monitor dimensions
		final Answer<Void> answer = inv -> {

			return null;
		};
		doAnswer(answer).when(lib).glfwGetMonitorPhysicalSize(factory.pointer().getValue(), count, count);

		// Init monitor name
		when(lib.glfwGetMonitorName(handle)).thenReturn("name");
//		when(lib.glfwGetMonitorPhysicalSize(factory.pointer().getValue(), count, count)).thenReturn();
		// Init monitor display modes
		when(lib.glfwGetVideoModes(handle, count)).thenReturn(struct);
//		doAnswer(CALLS_REAL_METHODS)

		// Enumerate monitors
		assertEquals(List.of(monitor), Monitor.monitors(desktop));
	}
}
