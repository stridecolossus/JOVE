package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
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
	void properties() {
		final var props = surface.properties(physical);
		assertNotNull(props);
	}

	@Nested
	class PropertiesTests {
		private Properties props;

		@BeforeEach
		void before() {
			props = surface.properties(physical);
		}

		@Test
		void capabilities() {
			final var caps = props.capabilities();
			assertNotNull(caps);
			verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, surface, caps);
		}

		@Test
		void formats() {
			final var formats = props.formats();
			assertNotNull(formats);
			verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(eq(physical), eq(surface), isA(IntByReference.class), isA(VkSurfaceFormatKHR.class));
		}

		@Test
		void modes() {
			final var modes = props.modes();
			assertNotNull(modes);
			verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(eq(physical), eq(surface), isA(IntByReference.class), isA(int[].class));
		}
	}

	@Test
	void close() {
		surface.close();
		verify(lib).vkDestroySurfaceKHR(instance, surface, null);
	}
}
