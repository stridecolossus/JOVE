package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Builder.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.*;

class LogicalDeviceTest {
	@SuppressWarnings("unused")
	static class MockLogicalDeviceLibrary extends MockLibrary {
		public VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device) {
			// Check descriptor
			assertEquals(VkStructureType.DEVICE_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(1, pCreateInfo.queueCreateInfoCount);

			// Check device features
			assertEquals(true, pCreateInfo.pEnabledFeatures[0].wideLines);

			// Check extensions
			assertEquals(1, pCreateInfo.enabledExtensionCount);
			assertArrayEquals(new String[]{"extension"}, pCreateInfo.ppEnabledExtensionNames);

			// Check validation layers
			assertEquals(1, pCreateInfo.enabledLayerCount);
			assertArrayEquals(new String[]{DiagnosticHandler.STANDARD_VALIDATION}, pCreateInfo.ppEnabledLayerNames);

			// Check required queues
			final VkDeviceQueueCreateInfo queue = pCreateInfo.pQueueCreateInfos[0];
			assertEquals(new EnumMask<>(0), queue.flags);
			assertEquals(1, queue.queueCount);
			assertEquals(0, queue.queueFamilyIndex);
			assertArrayEquals(new float[]{1}, queue.pQueuePriorities);

			// Create device
			init(device);
			return VkResult.VK_SUCCESS;
		}

		public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
			assertEquals(0, queueFamilyIndex);
			assertEquals(0, queueIndex);
			init(pQueue);
		}
	}

	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(LogicalDevice.Library.class);
		mockery.implement(new MockLogicalDeviceLibrary());

		device = new LogicalDevice.Builder(new MockPhysicalDevice())
				.layer(DiagnosticHandler.STANDARD_VALIDATION)
				.extension("extension")
				.feature("wideLines")
				.queue(new RequiredQueue(MockPhysicalDevice.FAMILY))
				.build(mockery.proxy());
	}

	@Test
	void constructor() {
		assertNotNull(device.limits());
		assertEquals(1, device.queues().size());
		assertFalse(device.isDestroyed());
	}

	// TODO - library cast fiddle

	@Test
	void queues() {
		final WorkQueue queue = device.queue(MockPhysicalDevice.FAMILY);
		assertEquals(MockPhysicalDevice.FAMILY, queue.family());
	}

	@Test
	void waitIdleDevice() {
		device.waitIdle();
		assertEquals(1, mockery.mock("vkDeviceWaitIdle").count());
	}

	@Test
	void waitIdleQueue() {
		final WorkQueue queue = device.queue(MockPhysicalDevice.FAMILY);
		device.waitIdle(queue);
		assertEquals(1, mockery.mock("vkQueueWaitIdle").count());
	}

	@Test
	void destroy() {
		device.destroy();
		assertTrue(device.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyDevice").count());
	}

	@DisplayName("A required queue...")
	@Nested
	class RequiredQueueTest {
		private LogicalDevice.Builder builder;
		private PhysicalDevice physical;

		@BeforeEach
		void before() {
			physical = new MockPhysicalDevice();
			builder = new LogicalDevice.Builder(physical);
		}

		@DisplayName("must be a member of the physical device")
		@Test
		void unknown() {
			final var unknown = new Family(1, 2, Set.of());
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(unknown, 1)));
		}

		@DisplayName("cannot exceed the maximum number of queues for that family")
		@Test
		void maximum() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(MockPhysicalDevice.FAMILY, 2)));
		}

		@DisplayName("can be specified by a selector")
		@Test
		void selector() {
			final Selector selector = new Selector((_, _) -> true);
			selector.test(physical);
			builder.queue(selector);
		}

		@DisplayName("with a duplicate queue family is silently ignored")
		@Test
		void duplicate() {
			builder.queue(new RequiredQueue(MockPhysicalDevice.FAMILY, 1));
			builder.queue(new RequiredQueue(MockPhysicalDevice.FAMILY, 1));
		}
	}
}
