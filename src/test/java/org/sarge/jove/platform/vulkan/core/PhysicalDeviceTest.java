package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceType;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Supported;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;

	@BeforeEach
	void before() {
		// Create Vulkan
		lib = mock(VulkanLibrary.class);

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
	void features() {
		final var features = dev.features();
		assertNotNull(features);
		verify(lib).vkGetPhysicalDeviceFeatures(eq(dev.handle()), any());
	}

	@Test
	void properties() {
		// Init properties
		final Answer<Void> answer = inv -> {
			final VkPhysicalDeviceProperties props = inv.getArgument(1);
			props.deviceName = "device".getBytes();
			props.deviceType = VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
			return null;
		};
		doAnswer(answer).when(lib).vkGetPhysicalDeviceProperties(eq(dev.handle()), any());

		// Retrieve properties
		final var props = dev.properties();
		assertNotNull(props);
		clearInvocations(lib);

		// Check cached
		assertEquals(props, dev.properties());
		verifyNoInteractions(lib);

		// Check properties
		assertEquals("device", dev.properties().name());
		assertEquals(VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, dev.properties().type());
		assertNotNull(props.limits());
	}

	@Test
	void supported() {
		// Init library
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().integer()).thenReturn(new IntByReference());

		// Check supported
		final Supported supported = dev.supported();
		assertNotNull(supported);
		assertEquals(Set.of(), supported.extensions());
		assertEquals(Set.of(), supported.layers());
	}
}
