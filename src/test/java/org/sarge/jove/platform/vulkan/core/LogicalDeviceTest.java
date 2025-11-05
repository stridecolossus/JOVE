package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.InstanceTest.MockInstanceLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDeviceTest.MockPhysicalDeviceLibrary;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class LogicalDeviceTest {
	/**
	 *
	 */
	static class MockLogicalDeviceLibrary implements LogicalDevice.Library {
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
			assertArrayEquals(new String[]{Vulkan.STANDARD_VALIDATION}, pCreateInfo.ppEnabledLayerNames);

			// Check required queues
			final VkDeviceQueueCreateInfo queue = pCreateInfo.pQueueCreateInfos[0];
			assertEquals(new EnumMask<>(0), queue.flags);
			assertEquals(1, queue.queueCount);
			assertEquals(0, queue.queueFamilyIndex);
			assertArrayEquals(new float[]{1}, queue.pQueuePriorities);

			// Create device
			device.set(new Handle(2));

			return VkResult.SUCCESS;
		}

		@Override
		public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
			assertEquals(0, queueFamilyIndex);
			assertEquals(0, queueIndex);
			pQueue.set(new Handle(3));
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
	private MockLogicalDeviceLibrary lib;

	@BeforeEach
	void before() {
		lib = new MockLogicalDeviceLibrary();
		family = new Family(0, 1, Set.of());
		queue = new WorkQueue(new Handle(3), family);
		device = new LogicalDevice(new Handle(2), lib, Map.of(family, List.of(queue)));
	}

	@Test
	void queues() {
		assertEquals(Map.of(family, List.of(queue)), device.queues());
	}

	@Test
	void waitIdleDevice() {
		device.waitIdle();
		assertEquals(true, lib.blocked);
	}

	@Test
	void waitIdleQueue() {
		device.waitIdle(queue);
		assertEquals(true, lib.queueBlocked);
	}

	@Test
	void builder() {
		final var instance = new Instance(new Handle(1), new MockInstanceLibrary());
		final PhysicalDevice parent = new PhysicalDevice(new Handle(1), List.of(family), instance, new MockPhysicalDeviceLibrary());

		device = new LogicalDevice.Builder(parent)
				.layer(Vulkan.STANDARD_VALIDATION)
				.extension("extension")
				.feature("wideLines")
				.queue(new RequiredQueue(family))
				.build(lib);

		assertEquals(false, lib.destroyed);
		assertEquals(false, device.isDestroyed());
		assertEquals(new Handle(2), device.handle());
		assertEquals(Map.of(family, List.of(queue)), device.queues());
	}

	@Test
	void destroy() {
		device.destroy();
		assertEquals(true, device.isDestroyed());
		assertEquals(true, lib.destroyed);
	}
}
