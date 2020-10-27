package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.DesktopLibrary.ErrorCallback;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;

public class DesktopTest {
	private Desktop desktop;
	private DesktopLibrary lib;

	@BeforeEach
	public void before() {
		lib = mock(DesktopLibrary.class);
		desktop = new Desktop(lib);
	}

	@Test
	public void version() {
		final String ver = "ver";
		when(lib.glfwGetVersionString()).thenReturn(ver);
		assertEquals(ver, desktop.version());
	}

	@Test
	public void isVulkanSupported() {
		when(lib.glfwVulkanSupported()).thenReturn(true);
		assertEquals(true, desktop.isVulkanSupported());
	}

	@Test
	void poll() {
		desktop.poll();
		verify(lib).glfwPollEvents();
	}

	@Test
	public void extensions() {
		final String[] extensions = {"one", "two"};
		final Answer<Pointer> answer = inv -> {
			final IntByReference count = inv.getArgument(0);
			count.setValue(extensions.length);
			return new StringArray(extensions);
		};
		doAnswer(answer).when(lib).glfwGetRequiredInstanceExtensions(isA(IntByReference.class));
		assertArrayEquals(extensions, desktop.extensions());
	}

	@Test
	public void setErrorHandler() {
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
	public void monitors() {
		// TODO
	}

	@Test
	public void window() {
		final Window.Descriptor descriptor = new Window.Descriptor.Builder().title("title").size(new Dimensions(1, 2)).build();
		when(lib.glfwCreateWindow(1, 2, "title", null, null)).thenReturn(new Pointer(42));
		final Window window = desktop.window(descriptor);
		assertNotNull(window);
	}

//	@Test
//	public void windowFullScreen() {
//		final Pointer handle = new Pointer(42);
//		final Monitor.DisplayMode mode = new Monitor.DisplayMode(new Dimensions(3, 4), new int[]{1, 2, 3}, 60);
//		final Monitor monitor = new Monitor(handle, "name", new Dimensions(1, 2), List.of(mode));
//		final WindowDescriptor props = new WindowDescriptor.Builder().title("title").size(new Dimensions(1, 2)).monitor(monitor).build();
//		when(instance.glfwCreateWindow(1, 2, "title", new Handle(handle), null)).thenReturn(new Pointer(42));
//		final Window window = service.window(props);
//		assertNotNull(window);
//	}

	@Test
	public void destroy() {
		desktop.destroy();
		verify(lib).glfwTerminate();
	}

	@Tag("GLFW")
	@Test
	public void create() {
		final Desktop desktop = Desktop.create();
		desktop.destroy();
	}
}
