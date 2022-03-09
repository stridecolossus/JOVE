package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceType;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Properties;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;
	private Family family;

	@BeforeEach
	void before() {
		// Create an instance
		instance = mock(Instance.class);

		// Init Vulkan
		lib = mock(VulkanLibrary.class);
		when(instance.library()).thenReturn(lib);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(factory.integer()).thenReturn(new IntByReference());
		when(instance.factory()).thenReturn(factory);

		// Create a queue family
		family = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));

		// Create device
		dev = new PhysicalDevice(new Pointer(42), instance, List.of(family));
	}

	@Test
	void constructor() {
		assertNotNull(dev.handle());
		assertEquals(instance, dev.instance());
		assertEquals(List.of(family), dev.families());
	}

	@Test
	void features() {
		final var features = dev.features();
		assertNotNull(features);
		verify(lib).vkGetPhysicalDeviceFeatures(eq(dev), any());
	}

	@Test
	void format() {
		final VkFormatProperties props = dev.properties(VkFormat.D32_SFLOAT);
		verify(lib).vkGetPhysicalDeviceFormatProperties(dev, VkFormat.D32_SFLOAT, props);
	}

	@Nested
	class PropertiesTest {
		private Properties props;
		private VkPhysicalDeviceLimits limits;

		@BeforeEach
		void before() {
			// Init device limits
			limits = new VkPhysicalDeviceLimits();

			// Init properties
			final Answer<Void> answer = inv -> {
				final VkPhysicalDeviceProperties props = inv.getArgument(1);
				props.deviceName = "device".getBytes();
				props.pipelineCacheUUID = "cache".getBytes();
				props.deviceType = VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
				props.limits = limits;
				return null;
			};
			doAnswer(answer).when(lib).vkGetPhysicalDeviceProperties(eq(dev), any(VkPhysicalDeviceProperties.class));

			// Retrieve properties
			props = dev.properties();
		}

		@Test
		void constructor() {
			assertNotNull(props);
			assertEquals("device", props.name());
			assertEquals("cache", props.cache());
			assertEquals(VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, props.type());
		}

		@Test
		void cached() {
			assertSame(props, dev.properties());
		}

		@Test
		void limits() {
			assertNotNull(props.limits());
			assertNotSame(limits, props.limits());
		}
	}

	@Nested
	class SelectorTest {
		@Test
		void failed() {
			final Selector selector = new Selector((dev, family) -> false);
			assertEquals(false, selector.test(dev));
			assertThrows(NoSuchElementException.class, () -> selector.family());
		}

		@Test
		void flags() {
			final Selector selector = Selector.of(Set.of(VkQueueFlag.GRAPHICS));
			assertNotNull(selector);
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.family());
		}

		@Test
		void presentation() {
			// Create presentation selector
			final Surface surface = mock(Surface.class);
			final Selector selector = Selector.of(surface);
			assertNotNull(selector);

			// Init supported boolean
			final IntByReference supported = new IntByReference(1);
			when(instance.factory().integer()).thenReturn(supported);

			// Check presentation queue
			assertEquals(true, selector.test(dev));
			assertEquals(family, selector.family());

			// Check API
			verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(dev, family.index(), surface, supported);
		}
	}

	@Test
	void enumerate() {
		// Init number of results
		final IntByReference count = new IntByReference(1);
		when(instance.factory().integer()).thenReturn(count);

		// Return the device handle
		final Pointer handle = new Pointer(1);
		final Answer<Integer> answer = inv -> {
			final Pointer[] array = inv.getArgument(2);
			array[0] = handle;
			return 0;
		};
		doAnswer(answer).when(lib).vkEnumeratePhysicalDevices(instance, count, new Pointer[1]);

		// Return the queue families for this device
		final Answer<Integer> families = inv -> {
			final VkQueueFamilyProperties props = inv.getArgument(2);
			props.queueCount = 1;
			props.queueFlags = VkQueueFlag.GRAPHICS.value();
			return 0;
		};
		doAnswer(families).when(lib).vkGetPhysicalDeviceQueueFamilyProperties(eq(handle), eq(count), any(VkQueueFamilyProperties.class));

		// Enumerate devices
		final Stream<PhysicalDevice> stream = PhysicalDevice.devices(instance);
		assertNotNull(stream);

		// Retrieve device
		final List<PhysicalDevice> list = stream.collect(toList());
		assertEquals(1, list.size());

		// Check device
		final PhysicalDevice dev = list.get(0);
		assertNotNull(dev);
		assertEquals(new Handle(handle), dev.handle());
		assertEquals(instance, dev.instance());

		// Check queue families
		final Family expected = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));
		assertEquals(List.of(expected), dev.families());
	}
}
