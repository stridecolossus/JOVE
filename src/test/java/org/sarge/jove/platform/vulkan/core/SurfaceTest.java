package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class SurfaceTest extends AbstractVulkanTest {
	private Surface surface;
	private Instance instance;
	private PhysicalDevice physical;

	@BeforeEach
	void before() {
		// Create instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create physical device
		physical = mock(PhysicalDevice.class);
		when(physical.instance()).thenReturn(instance);

		// Create surface
		surface = new Surface(new Handle(new Pointer(42)), physical);
	}

	@Test
	void constructor() {
		assertNotNull(surface.handle());
	}

	@Test
	void capabilities() {
		final var caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical.handle(), surface.handle(), caps);
	}

	@Test
	void formats() {
		final var formats = surface.formats();
		assertNotNull(formats);
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(eq(physical.handle()), eq(surface.handle()), isA(IntByReference.class), isA(VkSurfaceFormatKHR.class));
	}

	@Test
	void modes() {
		final var modes = surface.modes();
		assertNotNull(modes);
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(eq(physical.handle()), eq(surface.handle()), isA(IntByReference.class), isA(int[].class));
	}

	@Test
	void destroy() {
		surface.close();
		verify(lib).vkDestroySurfaceKHR(instance.handle(), surface.handle(), null);
	}
}
