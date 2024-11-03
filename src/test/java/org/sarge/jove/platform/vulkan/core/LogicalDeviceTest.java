package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.*;
import org.sarge.lib.Percentile;

public class LogicalDeviceTest {
	private LogicalDevice device;
	private PhysicalDevice parent;
	private WorkQueue queue;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init Vulkan
		lib = mock(VulkanLibrary.class);

		// Create parent physical device
		final WorkQueue.Family family = new Family(1, 2, Set.of());
		final Instance instance = new Instance(new Handle(1), lib, new MockReferenceFactory());
		final var supported = new SupportedFeatures(new VkPhysicalDeviceFeatures());
		parent = new PhysicalDevice(new Handle(2), instance, List.of(family), supported);

		// Create logical device
		queue = new WorkQueue(new Handle(3), family);
		device = new LogicalDevice(new Handle(4), parent, new DeviceFeatures(Set.of()), new VkPhysicalDeviceLimits(), Map.of(family, List.of(queue)));
	}

	@Test
	void constructor() {
		assertEquals(false, device.isDestroyed());
		assertEquals(parent, device.parent());
		assertEquals(lib, device.library());
		assertEquals(parent.instance().factory(), device.factory());
	}

	@DisplayName("A logical device has a set of enabled features")
	@Test
	void features() {
		assertNotNull(device.features());
	}

	@DisplayName("A logical device has a set of work queues")
	@Test
	void queues() {
		assertEquals(Map.of(queue.family(), List.of(queue)), device.queues());
	}

	@DisplayName("A work queue for a given familt can be retrieved from the logical device")
	@Test
	void queue() {
		assertEquals(queue, device.queue(queue.family()));
	}

	@DisplayName("A work queue can be blocked until all work has completed")
	@Test
	void queueWaitIdle() {
		queue.waitIdle(lib);
		verify(lib).vkQueueWaitIdle(queue);
	}

	@DisplayName("A logical device can be blocked until all work has completed")
	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device);
	}

	@DisplayName("A logical device can be destroyed")
	@Test
	void destroy() {
		device.destroy();
		verify(lib).vkDestroyDevice(device, null);
	}

	@DisplayName("A required queue...")
	@Nested
	class RequiredQueueTests {
		@DisplayName("must have at least one queue priority")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new RequiredQueue(queue.family(), List.of()));
		}

		@DisplayName("cannot specify more queues than the available number in the family")
		@Test
		void exceeds() {
			assertThrows(IllegalArgumentException.class, () -> new RequiredQueue(queue.family(), 3));
		}
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		void build() {
			// Create device
			device = builder
					.queue(new RequiredQueue(queue.family(), List.of(Percentile.HALF, Percentile.ONE)))
					.extension("ext")
					.layer(ValidationLayer.STANDARD_VALIDATION)
					.feature("samplerAnisotropy")
					.build();

			// Check device
			assertEquals(false, device.isDestroyed());
			assertEquals(parent, device.parent());
			assertEquals(1, device.queues().size());
			assertEquals(2, device.queues().get(queue.family()).size());
			assertEquals(new DeviceFeatures(Set.of("samplerAnisotropy")), device.features());

			// Check API
			final var expected = new VkDeviceCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var actual = (VkDeviceCreateInfo) obj;
					assertEquals(0, actual.flags);

					// Check device features
					assertEquals(true, actual.pEnabledFeatures.samplerAnisotropy);

					// Check extensions
					assertEquals(1, actual.enabledExtensionCount);
					assertNotNull(actual.ppEnabledExtensionNames);

					// Check validation layers
					assertEquals(1, actual.enabledLayerCount);
					assertNotNull(actual.ppEnabledLayerNames);

					// Check required queues
					assertEquals(1, actual.queueCreateInfoCount);
					assertEquals(null, actual.pQueueCreateInfos.flags);
					assertEquals(2, actual.pQueueCreateInfos.queueCount);
					assertEquals(1, actual.pQueueCreateInfos.queueFamilyIndex);
					assertEquals(new PointerToFloatArray(new float[]{0.5f, 1f}), actual.pQueueCreateInfos.pQueuePriorities);

					return true;
				}
			};
			verify(lib).vkCreateDevice(parent, expected, null, device.factory().pointer());
		}

		@DisplayName("A required queue must specify a family that belongs to the logical device")
		@Test
		void invalidQueueFamily() {
			final Family other = new Family(3, 4, Set.of());
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(other)));
		}

		@DisplayName("A required extension must be available to the logical device")
		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(Handler.EXTENSION));
		}
	}
}
