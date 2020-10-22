package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class WindowTest {
	private Window window;
	private DesktopLibrary instance;
	private WindowDescriptor props;

	@BeforeEach
	public void before() {
		instance = mock(DesktopLibrary.class);
		props = new WindowDescriptor.Builder().title("title").size(new Dimensions(640, 480)).property(WindowDescriptor.Property.DECORATED).build();
		window = new Window(new Pointer(1), instance, props);
	}

	@Test
	public void constructor() {
		assertNotNull(window.handle());
		assertEquals(props, window.descriptor());
	}

	@Test
	public void poll() {
		window.poll();
		verify(instance).glfwPollEvents();
	}

	@Test
	public void surface() {
		// Init API
		final Handle vulkan = new Handle(new Pointer(42));
		final Pointer ptr = new Pointer(2);
		final Answer<Integer> answer = inv -> {
			final PointerByReference ref = inv.getArgument(3);
			ref.setValue(ptr);
			return 0;
		};
		doAnswer(answer).when(instance).glfwCreateWindowSurface(eq(vulkan), eq(window.handle()), isNull(), isA(PointerByReference.class));

		// Create surface
		final Handle surface = window.surface(vulkan);
		assertEquals(new Handle(ptr), surface);
	}

	@Test
	public void destroy() {
		window.destroy();
		verify(instance).glfwDestroyWindow(window.handle());
	}
}
