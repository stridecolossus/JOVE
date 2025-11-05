package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkPresentModeKHR.*;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;

class VulkanSurfaceTest {
	static class MockVulkanSurfaceLibrary implements VulkanSurface.Library {
		private VkSurfaceFormatKHR supported = new VkSurfaceFormatKHR();
		private boolean destroyed;

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, IntegerReference supported) {
			assertNotNull(device);
			assertNotNull(surface);
			supported.set(1);
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
			pSurfaceCapabilities.minImageCount = 2;
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkSurfaceFormatKHR[] formats) {
			assertNotNull(device);
			assertNotNull(surface);
			if(formats == null) {
				count.set(1);
			}
			else {
				formats[0] = supported;
			}
			return VkResult.SUCCESS;
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
			return VkResult.SUCCESS;
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
		device = new MockPhysicalDevice();
		library = new MockVulkanSurfaceLibrary();
		surface = new VulkanSurface(new Handle(3), device, library);
	}

	@Test
	void presentation() {
		final Selector selector = VulkanSurface.presentation(new Handle(3), library);
		assertEquals(true, selector.test(device));
		assertEquals(device.families().getFirst(), selector.family(device));
	}

	@Test
	void load() {
		final var loaded = surface.load();
		final var capabilities = loaded.capabilities();
		final var modes = loaded.modes();
		final var formats = loaded.formats();
		assertSame(capabilities, loaded.capabilities());
		assertSame(modes, loaded.modes());
		assertSame(formats, loaded.formats());
	}

	@Test
	void capabilities() {
		final VkSurfaceCapabilitiesKHR capabilities = surface.capabilities();
		assertEquals(2, capabilities.minImageCount);
	}

	@Nested
	class SurfaceFormatTest {
		private final Predicate<VkSurfaceFormatKHR> none = _ -> false;

    	@Test
    	void formats() {
    		assertEquals(1, surface.formats().size());
    	}

    	@Test
    	void supported() {
    		assertEquals(library.supported, surface.select(VulkanSurface.equals(library.supported), null));
    	}

    	@Test
    	void fallback() {
    		final var def = new VkSurfaceFormatKHR();
    		assertEquals(def, surface.select(none, def));
    	}

    	@Test
    	void neither() {
    		final var def = VulkanSurface.defaultSurfaceFormat();
    		final VkSurfaceFormatKHR selected = surface.select(none, null);
    		assertEquals(def.format, selected.format);
    		assertEquals(def.colorSpace, selected.colorSpace);
    	}

    	@Test
    	void def() {
    		final var def = VulkanSurface.defaultSurfaceFormat();
    		assertEquals(VkFormat.B8G8R8A8_UNORM, def.format);
    		assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, def.colorSpace);
    	}
	}

	@Nested
	class PresentationModeTest {
    	@Test
    	void modes() {
    		assertEquals(List.of(FIFO_KHR, MAILBOX_KHR), surface.modes());
    	}

    	@Test
    	void select() {
    		assertEquals(MAILBOX_KHR, surface.select(List.of(MAILBOX_KHR)));
    		assertEquals(FIFO_KHR, surface.select(List.of(FIFO_KHR)));
    		assertEquals(FIFO_KHR, surface.select(List.of(VkPresentModeKHR.FIFO_LATEST_READY_KHR)));
    		assertEquals(FIFO_KHR, surface.select(List.of()));
    	}
	}

	@Test
	void destroy() {
		surface.destroy();
		assertEquals(true, surface.isDestroyed());
		assertEquals(true, library.destroyed);
	}
}
