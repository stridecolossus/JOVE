package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

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
	void features() {
		final var features = dev.features();
		verify(lib).vkGetPhysicalDeviceFeatures(dev.handle(), features);
	}

	@Test
	void memory() {
		final var mem = dev.memory();
		verify(lib).vkGetPhysicalDeviceMemoryProperties(dev.handle(), mem);
	}

	@Test
	void findMemoryType() {
		// Create a memory type
		final Set<VkMemoryPropertyFlag> flags = Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		final VkMemoryType type = new VkMemoryType();
		type.propertyFlags = IntegerEnumeration.mask(flags);

		// Create device memory properties
		final var props = new VkPhysicalDeviceMemoryProperties();
		props.memoryTypeCount = 2;
		props.memoryTypes = new VkMemoryType[]{new VkMemoryType(), type};

		// Mock memory properties
		final PhysicalDevice spy = spy(dev);
		doReturn(props).when(spy).memory();

		// Check memory type matched
		assertEquals(1, spy.findMemoryType(0b11, flags));

		// Check bit-wise filter
		assertThrows(RuntimeException.class, () -> dev.findMemoryType(0b01, flags));

		// Check property matching
		assertThrows(RuntimeException.class, () -> dev.findMemoryType(0b11, Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)));
		assertThrows(RuntimeException.class, () -> dev.findMemoryType(0b11, Set.of()));
	}
}
