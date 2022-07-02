package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
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

	@DisplayName("A surface has a descriptor of its supported capabilities")
	@Test
	void capabilities() {
		final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, surface, caps);
	}

	@DisplayName("A surface provides the supported surface formats")
	@Test
	void formats() {
		final List<VkSurfaceFormatKHR> formats = surface.formats();
		assertNotNull(formats);
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(physical, surface, INTEGER, formats.get(0));
	}

	@DisplayName("An available surface format can be selected for the surface")
	@Test
	void selectFormat() {
		final Surface cached = surface.cached();
		final VkSurfaceFormatKHR format = cached.formats().iterator().next();
		assertEquals(format, cached.format(format.format, format.colorSpace));
	}

	@DisplayName("A surface falls back to the default format for an unsupported surface format")
	@Test
	void selectFormatDefault() {
		final VkSurfaceFormatKHR expected = Surface.defaultSurfaceFormat();
		final VkSurfaceFormatKHR actual = surface.format(VkFormat.UNDEFINED, VkColorSpaceKHR.BT2020_LINEAR_EXT);
		assertEquals(expected.format, actual.format);
		assertEquals(expected.colorSpace, actual.colorSpace);
	}

	@DisplayName("A surface has a minimum default supported surface format")
	@SuppressWarnings("static-method")
	@Test
	void defaultSurfaceFormat() {
		final VkSurfaceFormatKHR format = Surface.defaultSurfaceFormat();
		assertNotNull(format);
		assertEquals(VkFormat.B8G8R8A8_UNORM, format.format);
		assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, format.colorSpace);
	}

	@DisplayName("A surface provides the set of supported presentation modes")
	@Test
	void modes() {
		final Set<VkPresentModeKHR> modes = surface.modes();
		final VkPresentModeKHR first = modes.iterator().next();
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(physical, surface, INTEGER, new int[]{first.value()});
	}

	@DisplayName("An available presentation mode can be selected for the surface")
	@Test
	void selectMode() {
		final VkPresentModeKHR mode = surface.modes().iterator().next();
		assertEquals(mode, surface.mode(mode));
	}

	@DisplayName("A surface falls back to the default for an unsupported presentation mode")
	@Test
	void selectModeDefault() {
		assertEquals(VkPresentModeKHR.FIFO_KHR, Surface.DEFAULT_PRESENTATION_MODE);
		assertEquals(Surface.DEFAULT_PRESENTATION_MODE, surface.mode(VkPresentModeKHR.SHARED_CONTINUOUS_REFRESH_KHR));
	}

	@DisplayName("The properties of the surface can be cached to minimise API calls")
	@Test
	void cached() {
		// Create cached instance
		final Surface cached = surface.cached();
		assertNotNull(cached);

		// Check capabilities are cached
		final VkSurfaceCapabilitiesKHR caps = cached.capabilities();
		assertEquals(caps, cached.capabilities());
		verify(lib, atMostOnce()).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, surface, caps);

		// Check surface formats are cached
		final List<VkSurfaceFormatKHR> formats = cached.formats();
		assertEquals(formats, cached.formats());
		verify(lib, atMostOnce()).vkGetPhysicalDeviceSurfaceFormatsKHR(physical, surface, INTEGER, formats.get(0));

		// Check presentation modes are cached
		final Set<VkPresentModeKHR> modes = cached.modes();
		assertEquals(modes, cached.modes());
		verify(lib, atMostOnce()).vkGetPhysicalDeviceSurfacePresentModesKHR(physical, surface, INTEGER, new int[]{modes.iterator().next().value()});
	}
}
