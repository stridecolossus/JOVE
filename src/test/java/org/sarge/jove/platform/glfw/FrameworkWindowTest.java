package org.sarge.jove.platform.glfw;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.Device;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.KeyListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.glfw.FrameworkLibraryDevice.MouseScrollListener;

import com.sun.jna.Pointer;

public class FrameworkWindowTest {
	private FrameworkWindow window;
	private FrameworkLibrary instance;
	private Window.Descriptor props;
	private Pointer handle;

	@BeforeEach
	public void before() {
		handle = new Pointer(42);
		instance = mock(FrameworkLibrary.class);
		props = new Window.Descriptor.Builder().title("title").size(new Dimensions(640, 480)).property(Window.Descriptor.Property.DECORATED).build();
		window = new FrameworkWindow(handle, instance, props);
	}

	@Test
	public void constructor() {
		assertEquals(props, window.descriptor());
	}

	@Test
	public void device() {
		final Device<?> device = window.device();
		assertNotNull(device);
		assertEquals(EnumSet.allOf(Event.Category.class), device.categories());
	}

	@Test
	public void deviceBindings() {
		final Event.Handler handler = mock(Event.Handler.class);
		for(Event.Category cat : Event.Category.values()) {
			window.device().bind(cat, handler);
		}
		verify(instance).glfwSetKeyCallback(eq(handle), any(KeyListener.class));
		verify(instance).glfwSetCursorPosCallback(eq(handle), any(MousePositionListener.class));
		verify(instance).glfwSetMouseButtonCallback(eq(handle), any(MouseButtonListener.class));
		verify(instance).glfwSetScrollCallback(eq(handle), any(MouseScrollListener.class));
	}

	@Test
	public void poll() {
		window.poll();
		verify(instance).glfwPollEvents();
	}

	@Test
	public void destroy() {
		window.destroy();
		verify(instance).glfwDestroyWindow(handle);
	}
}
