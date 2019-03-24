package org.sarge.jove.platform.glfw;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.Monitor;
import org.sarge.jove.platform.Service.ErrorHandler;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkLibrary.ErrorCallback;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class FrameworkDesktopServiceTest {
	private FrameworkDesktopService service;
	private FrameworkLibrary instance;

	@BeforeEach
	public void before() {
		instance = mock(FrameworkLibrary.class);
		service = new FrameworkDesktopService(instance);
	}

	@Test
	public void constructor() {
		assertEquals("GLFW", service.name());
	}

	@Test
	public void version() {
		final String ver = "ver";
		when(instance.glfwGetVersionString()).thenReturn(ver);
		assertEquals(ver, service.version());
	}

	@Test
	public void handler() {
		// Register handler
		final ErrorHandler handler = mock(ErrorHandler.class);
		final ArgumentCaptor<ErrorCallback> captor = ArgumentCaptor.forClass(ErrorCallback.class);
		service.handler(handler);
		verify(instance).glfwSetErrorCallback(captor.capture());

		// Generate error
		final ErrorCallback callback = captor.getValue();
		callback.error(42, "error");
		verify(handler).handle("GLFW error: code=42 [error]");
	}

	@Test
	public void isVulkanSupported() {
		when(instance.glfwVulkanSupported()).thenReturn(true);
		assertEquals(true, service.isVulkanSupported());
	}

	@Test
	public void monitors() {
		// TODO
	}

	@Test
	public void extensions() {
		// TODO - how? is this actually used anyway?
////		final String[] extensions = new String[]{"ext"};
//		final PointerByReference ref = new PointerByReference();
//		when(instance.glfwGetRequiredInstanceExtensions(any())).thenReturn(ref);
//		assertArrayEquals(new String[]{}, service.extensions());
	}

	@Test
	public void window() {
		final Window.Descriptor props = new Window.Descriptor.Builder().title("title").size(new Dimensions(1, 2)).build();
		when(instance.glfwCreateWindow(1, 2, "title", null, null)).thenReturn(new Pointer(42));
		final FrameworkWindow window = service.window(props);
		assertNotNull(window);
	}

	@Test
	public void windowFullScreen() {
		final Pointer handle = new Pointer(42);
		final Monitor.DisplayMode mode = new Monitor.DisplayMode(new Dimensions(3, 4), new int[]{1, 2, 3}, 60);
		final Monitor monitor = new Monitor(handle, "name", new Dimensions(1, 2), List.of(mode));
		final Window.Descriptor props = new Window.Descriptor.Builder().title("title").size(new Dimensions(1, 2)).monitor(monitor).build();
		when(instance.glfwCreateWindow(1, 2, "title", handle, null)).thenReturn(new Pointer(42));
		final FrameworkWindow window = service.window(props);
		assertNotNull(window);
	}

	@Test
	public void surface() {
		final Pointer vulkan = mock(Pointer.class);
		final Pointer window = mock(Pointer.class);
		service.surface(vulkan, window);
		verify(instance).glfwCreateWindowSurface(eq(vulkan), eq(window), isNull(), any(PointerByReference.class));
	}

	@Test
	public void close() {
		service.close();
		verify(instance).glfwTerminate();
	}

	@Tag("GLFW")
	@Test
	public void create() {
		final FrameworkDesktopService service = FrameworkDesktopService.create();
		service.close();
	}
}
