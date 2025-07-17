package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

public class DesktopTest {
	private Desktop desktop;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		desktop = new Desktop(lib);
	}

	@Test
	void library() {
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
		// TODO
//		final String[] extensions = {"ext"};
//		final IntegerReference count = factory.integer();
//		count.set(1);
//		when(lib.glfwGetRequiredInstanceExtensions(count)).thenReturn(new NativeArray(extensions, Arena.ofAuto()));
//		assertArrayEquals(extensions, desktop.extensions());
	}

//	@SuppressWarnings("unchecked")
//	@Test
//	void setErrorHandler() {
//		// Set error handler
//		final Consumer<String> handler = mock(Consumer.class);
//		desktop.setErrorHandler(handler);
//
//		// Check API
//		final ArgumentCaptor<ErrorCallback> captor = ArgumentCaptor.forClass(ErrorCallback.class);
//		verify(lib).glfwSetErrorCallback(captor.capture());
//
//		// Check handler
//		final ErrorCallback callback = captor.getValue();
//		callback.error(42, "doh");
//		verify(handler).accept("GLFW error: [42] doh");
//	}

	@Test
	void destroy() {
		assertEquals(false, desktop.isDestroyed());
		desktop.destroy();
		verify(lib).glfwTerminate();
	}
}
