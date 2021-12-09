package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.DesktopDisplayMode;
import org.sarge.jove.platform.desktop.Monitor.DisplayMode;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class MonitorTest {
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

	private static final IntByReference integer(int value) {
		return new IntByReference(value) {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
	}

	@Test
	void monitors() {
		// Init integer-by-reference return values
		final IntByReference count = integer(1);
		final var integers = List.of(count, integer(2), integer(3), count);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(factory.integer()).then(AdditionalAnswers.returnsElementsOf(integers));
		when(desktop.factory()).thenReturn(factory);

		// Init array of monitors
		final Pointer handle = new Pointer(1);
		final Pointer array = new Memory(8);
		array.setPointer(0, handle);
		when(lib.glfwGetMonitors(count)).thenReturn(array);

		// Init monitor name
		when(lib.glfwGetMonitorName(handle)).thenReturn("name");

		// Init monitor display modes
		when(lib.glfwGetVideoModes(handle, count)).thenReturn(struct);

		// Enumerate monitors
		assertEquals(List.of(monitor), Monitor.monitors(desktop));
	}
}
