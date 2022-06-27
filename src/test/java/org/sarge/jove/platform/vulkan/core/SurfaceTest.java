package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class SurfaceTest extends AbstractVulkanTest {
	private Surface surface;
	private Instance instance;
	private PhysicalDevice physical;

	@BeforeEach
	void before() {
		// Create instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);
		when(instance.factory()).thenReturn(factory);

		// Create physical device
		physical = mock(PhysicalDevice.class);
		when(physical.instance()).thenReturn(instance);

		// Create surface
		surface = new Surface(new Handle(1), physical);
	}

	@Test
	void constructor() {
		assertNotNull(surface.handle());
		assertEquals(false, surface.isDestroyed());
	}

	@Test
	void destroy() {
		surface.destroy();
		verify(lib).vkDestroySurfaceKHR(instance, surface, null);
	}

	@Test
	void capabilities() {
		final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, surface, caps);
	}

	@Test
	void formats() {
		final List<VkSurfaceFormatKHR> formats = surface.formats();
		assertNotNull(formats);
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(physical, surface, INTEGER, formats.get(0));
	}

	@Test
	void modes() {
		final Set<VkPresentModeKHR> modes = surface.modes();
		assertNotNull(modes);
		final VkPresentModeKHR first = modes.iterator().next();
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(physical, surface, INTEGER, new int[]{first.value()});
	}

	// TODO - format selector
}
