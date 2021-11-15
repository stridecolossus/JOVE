package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;
import org.sarge.jove.util.TestHelper.IntByReferenceMatcher;

import com.sun.jna.StringArray;

public class DesktopTest {
	private Desktop desktop;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		desktop = new Desktop(lib);
	}

	@Test
	void constructor() {
		assertEquals(lib, desktop.library());
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
		final String[] extensions = {"one", "two"};
		final IntByReferenceMatcher count = new IntByReferenceMatcher(2);
		when(lib.glfwGetRequiredInstanceExtensions(argThat(count))).thenReturn(new StringArray(extensions));
		assertArrayEquals(extensions, desktop.extensions());
	}

	@Test
	void setErrorHandler() {
		// Set error handler
		@SuppressWarnings("unchecked")
		final Consumer<String> handler = mock(Consumer.class);
		desktop.setErrorHandler(handler);

		// Check API
		final ArgumentCaptor<ErrorCallback> captor = ArgumentCaptor.forClass(ErrorCallback.class);
		verify(lib).glfwSetErrorCallback(captor.capture());

		// Check handler
		final ErrorCallback callback = captor.getValue();
		assertNotNull(callback);
		callback.error(42, "doh");
		verify(handler).accept("GLFW error: [42] doh");
	}

	@Test
	void destroy() {
		desktop.destroy();
		verify(lib).glfwTerminate();
	}

	@Disabled
	@Tag("GLFW")
	@Test
	void create() {
		final Desktop desktop = Desktop.create();
		desktop.destroy();
	}
}
