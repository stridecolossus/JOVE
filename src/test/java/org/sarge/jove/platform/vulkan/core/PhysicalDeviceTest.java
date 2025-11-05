package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceFeatures;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class PhysicalDeviceTest {
	static class MockPhysicalDeviceLibrary implements PhysicalDevice.Library {
		@Override
		public VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, Handle[] devices) {
			if(devices == null) {
				pPhysicalDeviceCount.set(1);
			}
			else {
				devices[0] = new Handle(1);
			}
			return VkResult.SUCCESS;
		}

		@Override
		public void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props) {
			props.deviceType = VkPhysicalDeviceType.DISCRETE_GPU;
		}

		@Override
		public void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties) {
		}

		@Override
		public void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features) {
			features.wideLines = true;
		}

		@Override
		public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] pQueueFamilyProperties) {
			if(pQueueFamilyProperties == null) {
				pQueueFamilyPropertyCount.set(1);
			}
			else {
				final var family = new VkQueueFamilyProperties();
				family.queueCount = 1;
				family.queueFlags = new EnumMask<>(VkQueueFlag.GRAPHICS);
				assertEquals(1, pQueueFamilyProperties.length);
				pQueueFamilyProperties[0] = family;
			}
		}

		@Override
		public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
			if(extensions == null) {
				count.set(1);
			}
			else {
				extensions[0] = new VkExtensionProperties();
			}
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, VkLayerProperties[] layers) {
			return VkResult.ERROR_DEVICE_LOST;
		}

		@Override
		public void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props) {
			props.linearTilingFeatures = new EnumMask<>(VkFormatFeature.COLOR_ATTACHMENT);		// TODO - ???
		}
	}

	private PhysicalDevice device;
	private Instance instance;
	private PhysicalDevice.Library library;
	private Family family;

	@BeforeEach
	void before() {
		instance = new MockInstance();
		family = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));
		library = new MockPhysicalDeviceLibrary();
		device = new PhysicalDevice(new Handle(2), List.of(family), instance, library);
	}

	@Test
	void properties() {
		final VkPhysicalDeviceProperties properties = device.properties();
		assertEquals(VkPhysicalDeviceType.DISCRETE_GPU, properties.deviceType);
	}

	@Test
	void families() {
		assertEquals(instance, device.instance());
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
		final var helper = new DeviceEnumerationHelper(instance, library);
		assertEquals(1, helper.enumerate().count());
	}

	@Test
	void selector() {
		final Selector selector = Selector.queue(Set.of(VkQueueFlag.GRAPHICS));
		assertEquals(true, selector.test(device));
		assertEquals(family, selector.family(device));
	}
}
