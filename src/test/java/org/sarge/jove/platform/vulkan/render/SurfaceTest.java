package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.MockReferenceFactory;

import com.sun.jna.ptr.IntByReference;

class SurfaceTest {
	private Surface surface;
	private PhysicalDevice dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init Vulkan
		lib = mock(VulkanLibrary.class);

		// Create instance
		final var instance = mock(Instance.class);
		when(instance.factory()).thenReturn(new MockReferenceFactory());
		when(instance.library()).thenReturn(lib);

		// Create device
		dev = mock(PhysicalDevice.class);
		when(dev.instance()).thenReturn(instance);

		// Create surface
		surface = new Surface(new Handle(1), dev);
	}

	@Test
	void destroy() {
		surface.destroy();
		verify(lib).vkDestroySurfaceKHR(dev.instance(), surface, null);
		assertEquals(true, surface.isDestroyed());
	}

	@DisplayName("A surface has a descriptor of its supported capabilities")
	@Test
	void capabilities() {
		final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, surface, caps);
	}

	@DisplayName("A surface provides the supported surface formats")
	@Test
	void formats() {
		final IntByReference count = dev.instance().factory().integer();
		final List<VkSurfaceFormatKHR> formats = surface.formats();
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(dev, surface, count, formats.get(0));
	}

	@DisplayName("An available surface format can be selected for the surface")
	@Test
	void selectFormat() {
		final Surface cached = surface.cached();
		final VkSurfaceFormatKHR format = cached.formats().iterator().next();
		assertEquals(format, cached.format(format.format, format.colorSpace, null));
	}

	@DisplayName("A surface falls back to the default format for an unsupported surface format")
	@Test
	void selectFormatDefault() {
		final VkSurfaceFormatKHR expected = Surface.defaultSurfaceFormat();
		final VkSurfaceFormatKHR actual = surface.format(VkFormat.UNDEFINED, VkColorSpaceKHR.BT2020_LINEAR_EXT, null);
		assertEquals(expected.format, actual.format);
		assertEquals(expected.colorSpace, actual.colorSpace);
	}

	@DisplayName("A surface has a minimum default supported surface format")
	@Test
	void defaultSurfaceFormat() {
		final VkSurfaceFormatKHR format = Surface.defaultSurfaceFormat();
		assertEquals(VkFormat.B8G8R8A8_UNORM, format.format);
		assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, format.colorSpace);
	}

	@DisplayName("A surface provides the set of supported presentation modes")
	@Test
	void modes() {
		final IntByReference count = dev.instance().factory().integer();
		final Set<VkPresentModeKHR> modes = surface.modes();
		final VkPresentModeKHR first = modes.iterator().next();
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(dev, surface, count, new int[]{first.value()});
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
		final VkSurfaceCapabilitiesKHR caps = cached.capabilities();
		assertEquals(caps, cached.capabilities());
		verify(lib, times(1)).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, surface, caps);

		// Check surface formats are cached
		final IntByReference count = dev.instance().factory().integer();
		final List<VkSurfaceFormatKHR> formats = cached.formats();
		assertEquals(formats, cached.formats());
		verify(lib, times(1)).vkGetPhysicalDeviceSurfaceFormatsKHR(dev, surface, count, formats.get(0));

		// Check presentation modes are cached
		final Set<VkPresentModeKHR> modes = cached.modes();
		assertEquals(modes, cached.modes());
		verify(lib, times(1)).vkGetPhysicalDeviceSurfacePresentModesKHR(dev, surface, count, new int[]{modes.iterator().next().value()});
	}
}
