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

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;

	@BeforeEach
	void before() {
		// Create Vulkan
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create an instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create a queue family
		final VkQueueFamilyProperties family = new VkQueueFamilyProperties();
		family.queueCount = 1;
		family.queueFlags = IntegerEnumeration.mask(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT);

		// Create device
		dev = new PhysicalDevice(new Pointer(42), instance, new VkQueueFamilyProperties[]{family});
	}

	@Test
	void constructor() {
		assertNotNull(dev.handle());
		assertEquals(instance, dev.instance());
		assertNotNull(dev.families());
		assertEquals(1, dev.families().size());
	}

	@Test
	void support() {
		assertNotNull(dev.extensions());
		assertNotNull(dev.layers());
	}

	@Test
	void properties() {
		final var props = dev.properties();
		verify(lib).vkGetPhysicalDeviceProperties(dev.handle(), props);
	}

	@Test
	void memory() {
		final var mem = dev.memory();
		verify(lib).vkGetPhysicalDeviceMemoryProperties(dev.handle(), mem);
	}

	@Test
	void features() {
		final var features = dev.features();
		verify(lib).vkGetPhysicalDeviceFeatures(dev.handle(), features);
	}

	@Nested
	class FamilyTests {
		private QueueFamily family;

		@BeforeEach
		void before() {
			family = dev.families().get(0);
		}

		@Test
		void constructor() {
			assertNotNull(family);
			assertEquals(1, family.count());
			assertEquals(Set.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT), family.flags());
			assertEquals(0, family.index());
		}

		@Test
		void equals() {
			assertEquals(true, family.equals(family));
			assertEquals(false, family.equals(null));
			assertEquals(false, family.equals(mock(QueueFamily.class)));
		}

		@Test
		void isPresentationSupported() {
			final Surface surface = mock(Surface.class);
			assertEquals(true, family.isPresentationSupported(surface));
			verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), 0, surface.handle(), lib.factory().integer());
		}
	}
}
