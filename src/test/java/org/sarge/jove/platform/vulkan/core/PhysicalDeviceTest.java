package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.*;

import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

import com.sun.jna.ptr.IntByReference;

class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;
	private Family family;
	private SupportedFeatures features;

	@BeforeEach
	void before() {
		// Init Vulkan
		lib = new MockVulkanLibrary();
		instance = new Instance(new Handle(2), lib);

		// Init supported features
		final var struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = true;
		features = new SupportedFeatures(struct);

		// Create device
		family = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));
		dev = new PhysicalDevice(new Handle(1), instance, List.of(family), features);
	}

	@Test
	void constructor() {
		assertEquals(instance, dev.instance());
		assertEquals(List.of(family), dev.families());
	}

	@DisplayName("A physical device can retrieve its properties")
	@Test
	void properties() {
		final VkPhysicalDeviceProperties props = dev.properties();
		assertNotNull(props);
		verify(lib).vkGetPhysicalDeviceProperties(dev, props);
	}

	@DisplayName("A physical device can retrieve the memory properties of the hardware")
	@Test
	void memory() {
		final VkPhysicalDeviceMemoryProperties props = dev.memory();
		assertNotNull(props);
		verify(lib).vkGetPhysicalDeviceMemoryProperties(dev, props);
	}

	@DisplayName("The supported extensions can be retrieved from a physical device")
	@Test
	void extensions() {
		dev.extensions();
		verify(lib).vkEnumerateDeviceExtensionProperties(dev, null, instance.factory().integer(), null);
	}

	@SuppressWarnings("deprecation")
	@DisplayName("The supported validation layers can be retrieved from a physical device")
	@Test
	void layers() {
		dev.layers();
		verify(lib).vkEnumerateDeviceLayerProperties(dev, instance.factory().integer(), null);
	}

	@DisplayName("A physical device has a set of supported features")
	@Test
	void features() {
		assertEquals(Set.of("samplerAnisotropy"), dev.features().features());
	}

	@DisplayName("A physical device can optionally support presentation")
	@Test
	void isPresentationSupported() {
		final Handle surface = new Handle(2);
		assertEquals(true, dev.isPresentationSupported(surface, family));
		verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(dev, 0, surface, instance.factory().integer());
	}

	@DisplayName("The properties of an image format can be queried from a physical device")
	@Test
	void format() {
		final VkFormatProperties props = dev.properties(VkFormat.D32_SFLOAT);
		assertNotNull(props);
		verify(lib).vkGetPhysicalDeviceFormatProperties(dev, VkFormat.D32_SFLOAT, props);
	}

	@DisplayName("The physical devices can be enumerated for a given instance")
	@Test
	void devices() {
		// Init device handle
		final IntByReference count = instance.factory().integer();
		final Answer<Integer> answer = inv -> {
			final Pointer[] array = inv.getArgument(2);
			array[0] = new Pointer(1);
			return 0;
		};
		doAnswer(answer).when(lib).vkEnumeratePhysicalDevices(instance, count, new Pointer[1]);

		// Init queue family
		final var arg = new VkQueueFamilyProperties() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		final Answer<Integer> families = inv -> {
			final VkQueueFamilyProperties props = inv.getArgument(2);
			props.queueCount = 1;
			props.queueFlags = EnumMask.of(VkQueueFlag.GRAPHICS);
			return 0;
		};
		doAnswer(families).when(lib).vkGetPhysicalDeviceQueueFamilyProperties(new Handle(1), count, arg);

		// Enumerate devices
		assertEquals(1, PhysicalDevice.enumerate(instance).count());
	}

	@DisplayName("A physical device can be matched by a required feature set")
	@Test
	void predicate() {
		final var required = new DeviceFeatures(Set.of("samplerAnisotropy"));
		final Predicate<PhysicalDevice> predicate = PhysicalDevice.predicate(required);
		assertNotNull(predicate);
		assertEquals(true, predicate.test(dev));
	}

	@DisplayName("A physical device selector...")
	@Nested
	class SelectorTests {
		@DisplayName("can be used to filter candidate devices")
		@SuppressWarnings("unchecked")
		@Test
		void test() {
			final BiPredicate<PhysicalDevice, Family> predicate = mock(BiPredicate.class);
			final Selector selector = new Selector(predicate);
			selector.test(dev);
			verify(predicate).test(dev, family);
		}

		@DisplayName("cannot select the queue family it the device does not match the selector")
		@SuppressWarnings("unchecked")
		@Test
		void select() {
			final Selector selector = new Selector(mock(BiPredicate.class));
			assertThrows(NoSuchElementException.class, () -> selector.select(dev));
		}

		@DisplayName("can be used to match a device that provides a specified queue family")
		@Test
		void queue() {
			final Selector selector = Selector.of(VkQueueFlag.GRAPHICS);
			assertNotNull(selector);
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.select(dev));
		}

		@DisplayName("can be used to match a device that supports presentation")
		@Test
		void presentation() {
			final Handle surface = new Handle(3);
			final Selector selector = Selector.of(surface);
			assertNotNull(selector);
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.select(dev));
		}
	}
}
