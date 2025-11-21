package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class LogicalDeviceTest {
	static class MockLogicalDeviceLibrary extends MockVulkanLibrary {
		boolean destroyed;
		boolean blocked;
		boolean queueBlocked;

		@Override
		public VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device) {
			// Check descriptor
			assertEquals(0, pCreateInfo.flags);
			assertEquals(1, pCreateInfo.queueCreateInfoCount);

			// Check device features
			assertEquals(true, pCreateInfo.pEnabledFeatures.wideLines);

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
			device.set(MemorySegment.ofAddress(2));

			return VkResult.SUCCESS;
		}

		@Override
		public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
			assertEquals(0, queueFamilyIndex);
			assertEquals(0, queueIndex);
			pQueue.set(MemorySegment.ofAddress(3));
		}

		@Override
		public void vkDestroyDevice(LogicalDevice device, Handle pAllocator) {
			destroyed = true;
		}

		@Override
		public VkResult vkDeviceWaitIdle(LogicalDevice device) {
			blocked = true;
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
			// TODO
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkQueueWaitIdle(WorkQueue queue) {
			queueBlocked = true;
			return VkResult.SUCCESS;
		}
	}

	private LogicalDevice device;
	private Family family;
	private WorkQueue queue;
	private MockLogicalDeviceLibrary library;

	@BeforeEach
	void before() {
		library = new MockLogicalDeviceLibrary();
		family = new Family(0, 1, Set.of());
		queue = new WorkQueue(new Handle(3), family);
		device = new LogicalDevice(new Handle(2), Map.of(family, List.of(queue)), new VkPhysicalDeviceLimits(), library);
	}

	@Test
	void queues() {
		assertEquals(Map.of(family, List.of(queue)), device.queues());
	}

	@Test
	void waitIdleDevice() {
		device.waitIdle();
		assertEquals(true, library.blocked);
	}

	@Test
	void waitIdleQueue() {
		device.waitIdle(queue);
		assertEquals(true, library.queueBlocked);
	}

	@Test
	void destroy() {
		device.destroy();
		assertEquals(true, device.isDestroyed());
		assertEquals(true, library.destroyed);
	}

	@Nested
	class BuilderTest {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			final var parent = new MockPhysicalDevice(library) {
				@Override
				public List<Family> families() {
					return List.of(family);
				}

				@Override
				public VkPhysicalDeviceProperties properties() {
					final var properties = new VkPhysicalDeviceProperties();
					properties.limits = new VkPhysicalDeviceLimits();
					return properties;
				}
			};
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		void build() {
			final LogicalDevice device = builder
					.layer(DiagnosticHandler.STANDARD_VALIDATION)
					.extension("extension")
					.feature("wideLines")
					.queue(new RequiredQueue(family))
					.build(library);

			assertEquals(false, library.destroyed);
			assertEquals(false, device.isDestroyed());
			assertEquals(new Handle(2), device.handle());
			assertEquals(Map.of(family, List.of(queue)), device.queues());
		}

		@DisplayName("A required queue must be a member of the physical device")
		@Test
		void unknown() {
			final var unknown = new Family(1, 2, Set.of());
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(unknown, 1)));
		}

		@DisplayName("A number of a required queue cannot exceed the maximum available for that family")
		@Test
		void maximum() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(family, 2)));
		}

		@DisplayName("Only one required queue can be specified for a given family")
		@Test
		void duplicate() {
			builder.queue(new RequiredQueue(family, 1));
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(family, 1)));
		}
	}
}
