package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceFeatures;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.VulkanSurfaceTest.MockVulkanSurfaceLibrary;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class PhysicalDeviceTest {
	static class MockPhysicalDeviceLibrary implements PhysicalDevice.Library {
		@Override
		public VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, Handle[] devices) {
			pPhysicalDeviceCount.set(1);

			if(devices != null) {
				assertEquals(1, devices.length);
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
			features.wideLines = 1;
		}

		@Override
		public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] props) {
			pQueueFamilyPropertyCount.set(1);

			if(props != null) {
				final var family = new VkQueueFamilyProperties();
				family.queueCount = 1;
				family.queueFlags = new EnumMask<>(VkQueueFlag.GRAPHICS);
				assertEquals(1, props.length);
				props[0] = family;
			}
		}

		@Override
		public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
			// TODO - what does this do?
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, VkLayerProperties[] layers) {
			return VkResult.ERROR_LAYER_NOT_PRESENT;
		}

		@Override
		public void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props) {
			props.linearTilingFeatures = new EnumMask<>(VkFormatFeature.COLOR_ATTACHMENT);		// TODO - ???
		}
	}

	private PhysicalDevice dev;
	private PhysicalDevice.Library lib;
	private Family family;

	@BeforeEach
	void before() {
		family = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));
		lib = new MockPhysicalDeviceLibrary();
		dev = new PhysicalDevice(new Handle(1), lib);
	}

	@Test
	void properties() {
		final VkPhysicalDeviceProperties props = dev.properties();
		// TODO - arrays!!!
//		assertEquals(VkPhysicalDeviceType.DISCRETE_GPU, props.deviceType);
	}

	@Test
	void families() {
		assertEquals(List.of(family), dev.families());
	}

	@Test
	void features() {
		assertEquals(new DeviceFeatures(Set.of("wideLines")), dev.features());
	}

	@Test
	void enumerate() {
		assertEquals(1, PhysicalDevice.enumerate(lib, null).count());
	}

	@DisplayName("A physical device selector...")
	@Nested
	class SelectorTests {
		@DisplayName("can choose a device with a given queue property")
		@Test
		void queue() {
			final Selector selector = Selector.family(VkQueueFlag.GRAPHICS);
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.select(dev));
		}

		@DisplayName("can choose a device that supports presentation to a given surface")
		@Test
		void presentation() {
			final var lib = new MockVulkanSurfaceLibrary() {
				@Override
				public VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, VulkanSurface surface, IntegerReference supported) {
					assertEquals(0, queueFamilyIndex);
					supported.set(1);
					return VkResult.SUCCESS;
				}
			};
			final var surface = new VulkanSurface(new Handle(2), new Handle(3), dev, lib);
			final Selector selector = Selector.presentation(surface);
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.select(dev));
		}
	}
}

//
//	@DisplayName("A physical device can retrieve the memory properties of the hardware")
//	@Test
//	void memory() {
//		final VkPhysicalDeviceMemoryProperties props = dev.memory();
//		assertNotNull(props);
//		verify(lib).vkGetPhysicalDeviceMemoryProperties(dev, props);
//	}
//
//	@DisplayName("The supported extensions can be retrieved from a physical device")
//	@Test
//	void extensions() {
//		dev.extensions();
//		verify(lib).vkEnumerateDeviceExtensionProperties(dev, null, instance.factory().integer(), null);
//	}
//
//	@SuppressWarnings("deprecation")
//	@DisplayName("The supported validation layers can be retrieved from a physical device")
//	@Test
//	void layers() {
//		dev.layers();
//		verify(lib).vkEnumerateDeviceLayerProperties(dev, instance.factory().integer(), null);
//	}
//
//	@DisplayName("A physical device has a set of supported features")
//	@Test
//	void features() {
//		assertEquals(Set.of("samplerAnisotropy"), dev.features().features());
//	}
//
//	@DisplayName("A physical device can optionally support presentation")
//	@Test
//	void isPresentationSupported() {
//		final Handle surface = new Handle(2);
//		assertEquals(true, dev.isPresentationSupported(surface, family));
//		verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(dev, 0, surface, instance.factory().integer());
//	}
//
//	@DisplayName("The properties of an image format can be queried from a physical device")
//	@Test
//	void format() {
//		final VkFormatProperties props = dev.properties(VkFormat.D32_SFLOAT);
//		assertNotNull(props);
//		verify(lib).vkGetPhysicalDeviceFormatProperties(dev, VkFormat.D32_SFLOAT, props);
//	}
//
//	@DisplayName("The physical devices can be enumerated for a given instance")
//	@Test
//	void devices() {
//		// Init device handle
//		final IntByReference count = instance.factory().integer();
//		final Answer<Integer> answer = inv -> {
//			final Pointer[] array = inv.getArgument(2);
//			array[0] = new Pointer(1);
//			return 0;
//		};
//		doAnswer(answer).when(lib).vkEnumeratePhysicalDevices(instance, count, new Pointer[1]);
//
//		// Init queue family
//		final var arg = new VkQueueFamilyProperties() {
//			@Override
//			public boolean equals(Object obj) {
//				return true;
//			}
//		};
//		final Answer<Integer> families = inv -> {
//			final VkQueueFamilyProperties props = inv.getArgument(2);
//			props.queueCount = 1;
//			props.queueFlags = EnumMask.of(VkQueueFlag.GRAPHICS);
//			return 0;
//		};
//		doAnswer(families).when(lib).vkGetPhysicalDeviceQueueFamilyProperties(new Handle(1), count, arg);
//
//		// Enumerate devices
//		assertEquals(1, PhysicalDevice.enumerate(instance).count());
//	}
//
//	@DisplayName("A physical device can be matched by a required feature set")
//	@Test
//	void predicate() {
//		final var required = new DeviceFeatures(Set.of("samplerAnisotropy"));
//		final Predicate<PhysicalDevice> predicate = PhysicalDevice.predicate(required);
//		assertNotNull(predicate);
//		assertEquals(true, predicate.test(dev));
//	}
