package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceFeatures;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.*;

class PhysicalDeviceTest {
	@SuppressWarnings("unused")
	static class MockPhysicalDeviceLibrary extends MockLibrary { // implements PhysicalDevice.Library {
		public VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, Handle[] devices) {
			pPhysicalDeviceCount.set(1);
			init(devices);
			return VkResult.VK_SUCCESS;
		}

		public void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props) {
			props.deviceType = VkPhysicalDeviceType.DISCRETE_GPU;
		}

		public void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features) {
			features.wideLines = true;
		}

		public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] pQueueFamilyProperties) {
			final var properties = new VkQueueFamilyProperties();
			properties.queueCount = 1;
			properties.queueFlags = new EnumMask<>(VkQueueFlags.GRAPHICS);
			init(pQueueFamilyProperties, properties);
			pQueueFamilyPropertyCount.set(1);
		}

		public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
			count.set(1);
			init(extensions, new VkExtensionProperties());
			return VkResult.VK_SUCCESS;
		}
	}

	private PhysicalDevice device;
	private Instance instance;
	private PhysicalDevice.Library library;
	private Family family;

	@BeforeEach
	void before() {
		final var mockery = new Mockery(PhysicalDevice.Library.class, Instance.Library.class);
		mockery.implement(new MockPhysicalDeviceLibrary());
		library = mockery.proxy();
		instance = new MockInstance() {
			@Override
			public Library library() {
				return (Instance.Library) library;
			}
		};
		family = new Family(0, 1, Set.of(VkQueueFlags.GRAPHICS));
		device = new PhysicalDevice(new Handle(2), List.of(family), library);
	}

	@Test
	void properties() {
		final VkPhysicalDeviceProperties properties = device.properties();
		assertEquals(VkPhysicalDeviceType.DISCRETE_GPU, properties.deviceType);
	}

	@Test
	void families() {
		assertEquals(List.of(family), device.families());
	}

	@Test
	void features() {
		assertEquals(new DeviceFeatures(Set.of("wideLines")), device.features());
	}

	@Test
	void extensions() {
		final VkExtensionProperties[] extensions = device.extensions(null);
		assertEquals(1, extensions.length);
		assertNotNull(extensions[0]);
	}

	@Test
	void enumerate() {
		final var devices = PhysicalDevice.enumerate(instance).toList();
		assertEquals(1, devices.size());
		assertNotNull(devices.getFirst());
	}

	@Test
	void selector() {
		final Selector selector = Selector.queue(VkQueueFlags.GRAPHICS);
		assertEquals(true, selector.test(device));
		assertEquals(family, selector.family(device));
	}
}
