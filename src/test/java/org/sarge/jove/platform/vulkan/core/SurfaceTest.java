package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class SurfaceTest extends AbstractVulkanTest {
	private Surface surface;
	private Handle handle;
	private Instance instance;

	@BeforeEach
	void before() {
		// Create instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create physical device
		final PhysicalDevice parent = mock(PhysicalDevice.class);
		when(parent.instance()).thenReturn(instance);
		when(dev.parent()).thenReturn(parent);

		// Create surface
		handle = new Handle(new Pointer(42));
		surface = new Surface(handle, dev);
	}

	@Test
	void constructor() {
		assertEquals(handle, surface.handle());
		assertEquals(dev, surface.device());
	}

	@Test
	void capabilities() {
		final var caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.parent().handle(), surface.handle(), caps);
	}

	@Test
	void formats() {
		final var formats = surface.formats();
		assertNotNull(formats);
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(eq(dev.parent().handle()), eq(surface.handle()), isA(IntByReference.class), isA(VkSurfaceFormatKHR.class));
	}

	@Test
	void modes() {
		final var modes = surface.modes();
		assertNotNull(modes);
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(eq(dev.parent().handle()), eq(surface.handle()), isA(IntByReference.class), isA(int[].class));
	}

	@Test
	void destroy() {
		surface.destroy();
		verify(lib).vkDestroySurfaceKHR(instance.handle(), handle, null);
	}
}
