package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.Event;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class WindowTest {
	private Window window;
	private DesktopLibrary lib;
	private Desktop desktop;

	@BeforeEach
	void before() {
		// Init native library
		lib = mock(DesktopLibrary.class);
		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(new Pointer(42));

		// Init desktop
		desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);

		// Init window descriptor
		final Window.Descriptor descriptor = new Window.Descriptor.Builder()
				.title("title")
				.size(new Dimensions(640, 480))
				.property(Window.Property.DECORATED)
				.build();

		// Create window
		window = Window.create(desktop, descriptor, null);
	}

	@Test
	void constructor() {
		// Check window
		assertNotNull(window.handle());
		assertEquals(desktop, window.desktop());

		// Check descriptor
		assertNotNull(window.descriptor());
		assertEquals("title", window.descriptor().title());
		assertEquals(new Dimensions(640, 480), window.descriptor().size());
		assertEquals(Set.of(Window.Property.DECORATED), window.descriptor().properties());

		// Check devices
		assertNotNull(window.keyboard());
		assertNotNull(window.mouse());

		// Check GLFW window hints applied
		verify(lib).glfwWindowHint(0x00020005, 1);
	}

// TODO
//	@Test
//	void createFailed() {
//		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(null);
//		assertThrows(RuntimeException.class, () -> new Window(desktop, null, null)
//
//
//		Window.create(lib, new Window.Descriptor("title", new Dimensions(640, 480), Set.of()), null));
//	}

	@Test
	void register() {
		final Consumer<Event> handler = mock(Consumer.class);
		window.register(handler, new Object());
	}

	@Test
	void surface() {
		// Init API
		final Handle vulkan = new Handle(new Pointer(42));
		final Pointer ptr = new Pointer(2);
		final Answer<Integer> answer = inv -> {
			final PointerByReference ref = inv.getArgument(3);
			ref.setValue(ptr);
			return 0;
		};
		doAnswer(answer).when(lib).glfwCreateWindowSurface(eq(vulkan), eq(window), isNull(), isA(PointerByReference.class));

		// Create surface
		final Handle surface = window.surface(vulkan);
		assertEquals(new Handle(ptr), surface);
	}

	@Test
	void destroy() {
		window.close();
		verify(lib).glfwDestroyWindow(window);
	}
}
