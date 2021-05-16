package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.memory.Allocator;
import org.sarge.jove.platform.vulkan.memory.MemoryType;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class LogicalDeviceTest {
	private static final String FEATURE = "samplerAnisotropy";

	private LogicalDevice device;
	private PhysicalDevice parent;
	private Queue.Family family;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().pointer()).thenReturn(new PointerByReference(new Pointer(1)));

		// Create instance
		final Instance instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create parent device
//		final Handle handle = new Handle(new Pointer(1));
		parent = mock(PhysicalDevice.class);
//		when(parent.handle()).thenReturn(handle);
		when(parent.instance()).thenReturn(instance);

		// Init supported features
		when(parent.features()).thenReturn(DeviceFeatures.of(Set.of(FEATURE)));

		// Create queue family
		family = mock(Queue.Family.class);
		when(family.count()).thenReturn(2);
		when(family.device()).thenReturn(parent);
		when(parent.families()).thenReturn(List.of(family));

		// Init supported features
		final var features = new VkPhysicalDeviceFeatures();
		features.samplerAnisotropy = VulkanBoolean.TRUE;

		//when(lib.vkGetPhysicalDeviceMemoryProperties(parent.handle(), props))

		// Create logical device
		device = new LogicalDevice.Builder(parent)
				.queues(family, List.of(Percentile.HALF, Percentile.ONE))
				.extension("ext")
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.features(features)
				.build();
	}

	@Test
	void constructor() {
		assertNotNull(device.handle());
		assertEquals(parent, device.parent());
		assertEquals(lib, device.library());
		assertEquals(false, device.isDestroyed());
	}

	@DisplayName("Query device for all available queues")
	@Test
	void queues() {
		// Check queues
		assertNotNull(device.queues());
		assertEquals(1, device.queues().size());
		assertEquals(true, device.queues().containsKey(family));

		// Check queues for family
		final var list = device.queues().get(family);
		assertNotNull(list);
		assertEquals(2, list.size());

		final Queue queue = list.get(0);
		assertNotNull(queue);
		assertEquals(family, queue.family());
		assertNotNull(queue.handle());
	}

	@DisplayName("Wait for queue to complete execution")
	@Test
	void queueWaitIdle() {
		final Queue queue = device.queues().get(family).get(0);
		queue.waitIdle();
		verify(lib).vkQueueWaitIdle(queue.handle());
	}

	@DisplayName("Wait for all queues to complete execution")
	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device.handle());
	}

	@DisplayName("Check features supported by the device")
	@Test
	void features() {
		final DeviceFeatures features = device.features();
		assertNotNull(features);
		assertEquals(true, features.isSupported(FEATURE));
	}

	@Nested
	class AllocationTests {
		private MemoryType type;

		@BeforeEach
		void before() {
			type = new MemoryType(0, new Heap(0, 1, Set.of()), Set.of());
		}

		@Test
		void allocate() {
//			final DeviceMemory mem = device.allocate(type, 2);
		}

		@Test
		void select() {
//			// Allocate memory
//			final DeviceMemory mem = device.allocate(reqs, props);
//			assertNotNull(mem);
//
//			// Check memory block
//			assertEquals(2, mem.size());
//			assertEquals(false, mem.isMapped());
//			assertEquals(false, mem.isDestroyed());
//
//			// Check API
//			final ArgumentCaptor<VkMemoryAllocateInfo> captor = ArgumentCaptor.forClass(VkMemoryAllocateInfo.class);
//			final PointerByReference ref = lib.factory().pointer();
//			verify(lib).vkAllocateMemory(eq(dev.handle()), captor.capture(), isNull(), eq(ref));
//
//			// Check memory descriptor
//			final VkMemoryAllocateInfo info = captor.getValue();
//			assertNotNull(info);
//			assertEquals(1, info.memoryTypeIndex);
//			assertEquals(2L, info.allocationSize);
		}

		@Test
		void allocator() {
			final Allocator allocator = mock(Allocator.class);
			device.allocator(allocator);
//			device.allocate(type, 1);
//			verify(allocator).allocate(type, 1);
		}
	}

	@DisplayName("Create a semaphore for this device")
	@Test
	void semaphore() {
		// Create semaphore
		final Semaphore semaphore = device.semaphore();
		assertNotNull(semaphore);

		// Check API
		final ArgumentCaptor<VkSemaphoreCreateInfo> captor = ArgumentCaptor.forClass(VkSemaphoreCreateInfo.class);
		verify(lib).vkCreateSemaphore(eq(device.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check create descriptor
		final VkSemaphoreCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);
	}

	@Test
	void destroy() {
		device.destroy();
		verify(lib).vkDestroyDevice(device.handle(), null);
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder(parent);
		}

		@DisplayName("Duplicate queues should be aggregated")
		@Test
		void duplicate() {
			builder.queue(family);
			builder.queue(family);
			device = builder.build();
			assertEquals(1, device.queues().get(family).size());
		}

		@DisplayName("Cannot request more queues than available")
		@Test
		void invalidQueueCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3));
		}

		@DisplayName("Cannot request a queue from a different device")
		@Test
		void invalidQueueFamily() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(Queue.Family.class)));
		}

		@DisplayName("Cannot request an extension that is not available")
		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS));
		}
	}
}
