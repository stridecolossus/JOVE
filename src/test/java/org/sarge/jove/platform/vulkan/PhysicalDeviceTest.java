package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private Vulkan vulkan;

	@BeforeEach
	void before() {
		// Create Vulkan
		vulkan = mock(Vulkan.class);
		when(vulkan.api()).thenReturn(mock(VulkanLibrary.class));

		// Create a queue family
		final VkQueueFamilyProperties family = new VkQueueFamilyProperties();
		family.queueCount = 1;
		family.queueFlags = IntegerEnumeration.mask(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT);

		// Create device
		dev = new PhysicalDevice(new Pointer(42), vulkan, new VkQueueFamilyProperties[]{family, family});
	}

	@Test
	void constructor() {
		assertNotNull(dev.handle());
		assertEquals(vulkan, dev.vulkan());
		assertNotNull(dev.families());
		assertEquals(2, dev.families().size());
		assertNotNull(dev.extensions());
		assertNotNull(dev.layers());
	}

	@Test
	void properties() {
		final var props = dev.properties();
		verify(vulkan.api()).vkGetPhysicalDeviceProperties(dev.handle(), props);
	}

	@Test
	void memory() {
		final var mem = dev.memory();
		verify(vulkan.api()).vkGetPhysicalDeviceMemoryProperties(dev.handle(), mem);
	}

	@Test
	void features() {
		final var features = dev.features();
		verify(vulkan.api()).vkGetPhysicalDeviceFeatures(dev.handle(), features);
	}

	@Nested
	class FamilyTests {
		private QueueFamily one, two;

		@BeforeEach
		void before() {
			one = dev.families().get(0);
			two = dev.families().get(1);
		}

		@Test
		void family() {
			assertNotNull(one);
			assertEquals(1, one.count());
			assertEquals(Set.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT), one.flags());
		}

		@Test
		void indices() {
			assertEquals(0, one.index());
			assertEquals(1, two.index());
		}

		@Test
		void equals() {
			assertEquals(true, one.equals(one));
			assertEquals(false, one.equals(null));
			assertEquals(false, one.equals(two));
		}

		@Test
		void isPresentationSupported() {
			final Surface surface = mock(Surface.class);
			final IntByReference ref = new IntByReference(1);
			when(vulkan.integer()).thenReturn(ref);
			assertEquals(true, one.isPresentationSupported(surface));
			verify(vulkan.api()).vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), 0, surface.handle(), ref);
		}
	}
}
