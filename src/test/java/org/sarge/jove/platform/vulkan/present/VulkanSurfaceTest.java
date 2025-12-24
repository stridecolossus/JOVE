package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkPresentModeKHR.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.desktop.MockWindow;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;

class VulkanSurfaceTest {
	static class MockVulkanSurfaceLibrary implements VulkanSurface.Library {
		private boolean destroyed;

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, VulkanSurface surface, IntegerReference supported) {
			assertNotNull(device);
			assertNotNull(surface);
			supported.set(1);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
			pSurfaceCapabilities.minImageCount = 2;
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkSurfaceFormatKHR[] formats) {
			assertNotNull(device);
			assertNotNull(surface);
			if(formats == null) {
				count.set(1);
			}
			else {
				final var supported = new VkSurfaceFormatKHR();
				supported.format = VkFormat.UNDEFINED;
				supported.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
				formats[0] = supported;
			}
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkPresentModeKHR[] modes) {
			assertNotNull(device);
			assertNotNull(surface);
			if(modes == null) {
				count.set(2);
			}
			else {
				modes[0] = FIFO_KHR;
				modes[1] = MAILBOX_KHR;
			}
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroySurfaceKHR(Instance instance, VulkanSurface surface, Handle allocator) {
			assertNotNull(instance);
			assertNotNull(surface);
			assertEquals(null, allocator);
			destroyed = true;
		}
	}

	private VulkanSurface surface;
	private MockVulkanSurfaceLibrary library;
	private PhysicalDevice device;

	@BeforeEach
	void before() {
		final var window = new MockWindow() {
			@Override
			public Handle surface(Handle instance) {
				return new Handle(42);
			}
		};
		device = new MockPhysicalDevice();
		library = new MockVulkanSurfaceLibrary();
		surface = new VulkanSurface(window, new MockInstance(), library);
	}

	@Test
	void presentation() {
		final Family family = device.families().getFirst();
		assertEquals(true, surface.isPresentationSupported(device, family));
	}

	@Test
	void destroy() {
		surface.destroy();
		assertEquals(true, surface.isDestroyed());
		assertEquals(true, library.destroyed);
	}

	@Nested
	class PropertiesTest {
		private Properties properties;

		@BeforeEach
		void before() {
			properties = surface.properties(device).get();
		}

    	@Test
    	void capabilities() {
    		final VkSurfaceCapabilitiesKHR capabilities = properties.capabilities();
    		assertEquals(2, capabilities.minImageCount);
    	}

    	@Test
    	void formats() {
    		assertEquals(1, properties.formats().size());
    	}

    	@Test
    	void modes() {
    		assertEquals(List.of(FIFO_KHR, MAILBOX_KHR), properties.modes());
    	}
    }
}
