package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkColorSpaceKHR;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.core.Surface.Properties;
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
		surface = new Surface(new Handle(1), instance);
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

	@Nested
	class PropertiesTests {
		private Properties props;

		@BeforeEach
		void before() {
			props = surface.properties(physical);
		}

		@Test
		void constructor() {
			assertNotNull(props);
			assertEquals(physical, props.device());
			assertEquals(surface, props.surface());
		}

		@Test
		void capabilities() {
			final VkSurfaceCapabilitiesKHR caps = props.capabilities();
			assertNotNull(caps);
			verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, surface, caps);
		}

		@Test
		void formats() {
			final List<VkSurfaceFormatKHR> formats = props.formats();
			assertNotNull(formats);
			props.formats();
			verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(physical, surface, INTEGER, formats.get(0));
		}

		@Test
		void formatSelector() {
			final VkSurfaceFormatKHR first = props.formats().get(0);
			final VkSurfaceFormatKHR result = props.format(FORMAT, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);
			assertEquals(first, result);
		}

		@Test
		void modes() {
			final Set<VkPresentModeKHR> modes = props.modes();
			assertNotNull(modes);
			props.modes();
			final VkPresentModeKHR first = modes.iterator().next();
			verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(physical, surface, INTEGER, new int[]{first.value()});
		}
	}
}
