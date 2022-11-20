package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;
import org.sarge.jove.util.*;

import com.sun.jna.StringArray;

public class DesktopTest {
	private Desktop desktop;
	private DesktopLibrary lib;
	private ReferenceFactory factory;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		factory = new MockReferenceFactory();
		desktop = new Desktop(lib, factory);
	}

	@Test
	void constructor() {
		assertEquals(lib, desktop.library());
		assertEquals(factory, desktop.factory());
	}

	@Test
	void version() {
		final String ver = "ver";
		when(lib.glfwGetVersionString()).thenReturn(ver);
		assertEquals(ver, desktop.version());
	}

	@Test
	void isVulkanSupported() {
		when(lib.glfwVulkanSupported()).thenReturn(true);
		assertEquals(true, desktop.isVulkanSupported());
	}

	@Test
	void poll() {
		desktop.poll();
		verify(lib).glfwPollEvents();
	}

	@Test
	void extensions() {
		final String[] extensions = {"ext"};
		when(lib.glfwGetRequiredInstanceExtensions(factory.integer())).thenReturn(new StringArray(extensions));
		assertArrayEquals(extensions, desktop.extensions());
	}

	@SuppressWarnings("unchecked")
	@Test
	void setErrorHandler() {
		// Set error handler
		final Consumer<String> handler = mock(Consumer.class);
		desktop.setErrorHandler(handler);

		// Check API
		final ArgumentCaptor<ErrorCallback> captor = ArgumentCaptor.forClass(ErrorCallback.class);
		verify(lib).glfwSetErrorCallback(captor.capture());

		// Check handler
		final ErrorCallback callback = captor.getValue();
		callback.error(42, "doh");
		verify(handler).accept("GLFW error: [42] doh");
	}

	@Test
	void destroy() {
		assertEquals(false, desktop.isDestroyed());
		desktop.destroy();
		verify(lib).glfwTerminate();
	}
}
