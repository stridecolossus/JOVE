package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class WindowTest {
	private Window window;
	private DesktopLibrary lib;
	private Window.Descriptor descriptor;

	@BeforeEach
	void before() {
		// Init native library
		lib = mock(DesktopLibrary.class);
		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(new Pointer(42));

		// Create window
		descriptor = new Window.Descriptor.Builder().title("title").size(new Dimensions(640, 480)).property(Window.Property.DECORATED).build();
		window = Window.create(lib, descriptor, null);
	}

	@Test
	void constructor() {
		assertNotNull(window.handle());
		assertEquals(descriptor, window.descriptor());
		assertNotNull(window.keyboard());
		assertNotNull(window.mouse());
		verify(lib).glfwWindowHint(0x00020005, 1);
	}

	@Test
	void createFailed() {
		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> Window.create(lib, descriptor, null));
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
		doAnswer(answer).when(lib).glfwCreateWindowSurface(eq(vulkan), eq(window.handle()), isNull(), isA(PointerByReference.class));

		// Create surface
		final Handle surface = window.surface(vulkan);
		assertEquals(new Handle(ptr), surface);
	}

	@Test
	void destroy() {
		window.destroy();
		verify(lib).glfwDestroyWindow(window.handle());
	}
}
