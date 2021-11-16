package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.core.Surface.Properties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

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
		when(instance.factory()).thenReturn(factory);

		// Create physical device
		physical = mock(PhysicalDevice.class);
		when(physical.instance()).thenReturn(instance);

		// Create surface
		surface = new Surface(new Handle(42), instance);
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
			verify(lib, atMostOnce()).vkGetPhysicalDeviceSurfaceFormatsKHR(eq(physical), eq(surface), isA(IntByReference.class), isA(VkSurfaceFormatKHR.class));
		}

		@Test
		void modes() {
			final Set<VkPresentModeKHR> modes = props.modes();
			assertNotNull(modes);
			props.modes();
			verify(lib, atMostOnce()).vkGetPhysicalDeviceSurfacePresentModesKHR(eq(physical), eq(surface), isA(IntByReference.class), isA(int[].class));
		}
	}
}
