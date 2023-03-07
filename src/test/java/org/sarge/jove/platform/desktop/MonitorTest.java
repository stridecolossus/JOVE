package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.DesktopDisplayMode;
import org.sarge.jove.platform.desktop.Monitor.DisplayMode;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

class MonitorTest {
	private Monitor monitor;
	private DisplayMode mode;
	private DesktopDisplayMode struct;
	private Desktop desktop;
	private DesktopLibrary lib;
	private ReferenceFactory factory;

	@BeforeEach
	void before() {
		// Init desktop service
		lib = mock(DesktopLibrary.class);
		factory = mock(ReferenceFactory.class);
		desktop = new Desktop(lib, factory);

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
		// Init reference factory
		final IntByReference count = new IntByReference(1);
		when(factory.integer()).thenReturn(count, new IntByReference(2), new IntByReference(3), count);

		// Init array of monitors
		final Pointer handle = new Pointer(1);
		final Pointer array = new Memory(8);
		array.setPointer(0, handle);
		when(lib.glfwGetMonitors(count)).thenReturn(array);

		// Init monitor properties
		when(lib.glfwGetMonitorName(handle)).thenReturn("name");
		when(lib.glfwGetVideoModes(handle, count)).thenReturn(struct);

		// Enumerate monitors
		assertEquals(List.of(monitor), Monitor.monitors(desktop));
	}
}
