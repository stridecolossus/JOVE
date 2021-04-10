package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.DeviceMemory;
import org.sarge.jove.common.DeviceMemory.Pool;
import org.sarge.jove.common.DeviceMemory.Pool.AllocationException;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryHeap;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.core.VulkanAllocator.Heap;
import org.sarge.jove.platform.vulkan.core.VulkanAllocator.Request;
import org.sarge.jove.platform.vulkan.core.VulkanAllocator.Type;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VulkanAllocatorTest extends AbstractVulkanTest {
	private static final VkMemoryPropertyFlag FLAG = VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
	private static final Set<VkMemoryPropertyFlag> FLAGS = Set.of(FLAG);

	private VulkanAllocator allocator;

	@BeforeEach
	void before() {
		// Create physical device
		final Handle handle = new Handle(new Pointer(1));
		final PhysicalDevice parent = mock(PhysicalDevice.class);
		when(parent.handle()).thenReturn(handle);
		when(dev.parent()).thenReturn(parent);

		// Create device properties
		final var props = mock(PhysicalDevice.Properties.class);
		when(parent.properties()).thenReturn(props);

		// Init global memory properties
		final var limits = new VkPhysicalDeviceLimits();
		limits.maxMemoryAllocationCount = 2;
		limits.bufferImageGranularity = 3;
		when(props.limits()).thenReturn(limits);

		// Init memory properties
		final Answer<Void> answer = inv -> {
			// Create heap
			final var heap = new VkMemoryHeap();
			heap.size = 4;
			heap.flags = IntegerEnumeration.mask(FLAGS);

			// Create memory type
			final var type = new VkMemoryType();
			type.heapIndex = 0;
			type.propertyFlags = IntegerEnumeration.mask(FLAGS);

			// Populate memory properties
			final VkPhysicalDeviceMemoryProperties mem = inv.getArgument(1);
			mem.memoryHeapCount = 1;
			mem.memoryHeaps = new VkMemoryHeap[]{heap};
			mem.memoryTypeCount = 1;
			mem.memoryTypes = new VkMemoryType[]{type};

			return null;
		};
		doAnswer(answer).when(lib).vkGetPhysicalDeviceMemoryProperties(eq(handle), isA(VkPhysicalDeviceMemoryProperties.class));

		// Create allocator
		allocator = new VulkanAllocator(dev);
	}

	@Test
	void constructor() {
		assertEquals(2, allocator.maximumAllocationCount());
	}

	@Test
	void heaps() {
		// Check heaps
		assertNotNull(allocator.heaps());
		assertEquals(1, allocator.heaps().size());

		// Check heap
		final Heap heap = allocator.heaps().get(0);
		assertNotNull(heap);
		assertEquals(0, heap.index());
		assertEquals(4, heap.size());
		assertEquals(FLAGS, heap.properties());

		// Check equality
		assertEquals(true, heap.equals(heap));
		assertEquals(false, heap.equals(null));
		assertEquals(false, heap.equals(mock(Heap.class)));

		// Check types
		final Type type = allocator.types().get(0);
		assertEquals(List.of(type), heap.types());
	}

	@Test
	void types() {
		// Check types
		assertNotNull(allocator.types());
		assertEquals(1, allocator.types().size());

		// Check type
		final Type type = allocator.types().get(0);
		assertEquals(0, type.index());
		assertEquals(FLAGS, type.properties());
		assertEquals(true, type.equals(type));
		assertEquals(false, type.equals(null));

		// Check heap
		final Heap heap = allocator.heaps().get(0);
		assertEquals(heap, type.heap());

		// Check pool
		final Pool pool = type.pool();
		assertNotNull(pool);
		assertEquals(0, pool.count());
		assertEquals(0, pool.free());
		assertEquals(0, pool.size());
	}

	@Nested
	class RequestTests {
		private Request request;

		@BeforeEach
		void before() {
			request = allocator.request();
		}

		@Test
		void constructor() {
			assertNotNull(request);
		}

		@Test
		void allocate() {
			// Allocate memory
			final DeviceMemory mem = request.size(1).required(FLAG).allocate();
			assertNotNull(mem);
			assertEquals(1, mem.size());
			assertEquals(0, mem.offset());
			assertNotNull(mem.handle());
			assertEquals(false, mem.isDestroyed());

			// Check pool
			final Pool pool = allocator.types().get(0).pool();
			assertEquals(1, pool.count());
			assertEquals(2, pool.free());
			assertEquals(3, pool.size());
			assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());

			// Check API
			final ArgumentCaptor<VkMemoryAllocateInfo> captor = ArgumentCaptor.forClass(VkMemoryAllocateInfo.class);
			verify(lib).vkAllocateMemory(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

			// Check allocation descriptor
			final VkMemoryAllocateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(0, info.memoryTypeIndex);
			assertEquals(3, info.allocationSize);
		}

		@Test
		void allocateZeroSize() {
			assertThrows(IllegalArgumentException.class, "Memory size not specified", () -> request.allocate());
		}

		@Test
		void allocateEmptyMemoryProperties() {
			request.size(1);
			assertThrows(AllocationException.class, "No memory type available", () -> request.allocate());
		}

		@Test
		void allocateTypeNotSupported() {
			request.size(1).required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
			assertThrows(AllocationException.class, "No memory type available", () -> request.allocate());
		}

		@Test
		void allocateOptimal() {
			final DeviceMemory mem = request.size(1).optimal(FLAG).allocate();
			assertNotNull(mem);
		}

		@Test
		void allocateFallback() {
			final DeviceMemory mem = request.size(1).optimal(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT).required(FLAG).allocate();
			assertNotNull(mem);
		}
	}
}
