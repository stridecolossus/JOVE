package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class AllocatorTest {
	private Allocator allocator;
	private MemoryType fallback, optimal;
	private VkMemoryRequirements reqs;
	private MemoryProperties<?> props;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		// Create some memory types
		final Heap heap = new Heap(0, Set.of());
		fallback = new MemoryType(0, heap, Set.of(VkMemoryProperty.HOST_VISIBLE));
		optimal = new MemoryType(1, heap, Set.of(VkMemoryProperty.HOST_VISIBLE, VkMemoryProperty.DEVICE_LOCAL));

		// Init a memory request matching all types
		reqs = new VkMemoryRequirements();
		reqs.size = 2;
		reqs.memoryTypeBits = 0b11;

		// Init memory properties for the optimal type
		props = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_VISIBLE)
        		.optimal(VkMemoryProperty.DEVICE_LOCAL)
        		.build();

		// Create allocator
		dev = new MockDeviceContext();
		allocator = new Allocator(dev, new MemoryType[]{fallback, optimal}, 1, 5);
	}

	@Test
	void constructor() {
		assertEquals(0, allocator.count());
		assertEquals(1, allocator.max());
		assertEquals(5, allocator.page());
	}

	@DisplayName("Memory can be allocated for a matching memory type")
	@Test
	void allocate() {
		final DeviceMemory mem = allocator.allocate(reqs, props);
		assertEquals(optimal, mem.type());
		assertEquals(2, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(1, allocator.count());
	}

	@Test
	void api() {
		final var expected = new VkMemoryAllocateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var info = (VkMemoryAllocateInfo) obj;
				assertEquals(5, info.allocationSize);
				assertEquals(1, info.memoryTypeIndex);
				return true;
			}
		};

		allocator.allocate(reqs, props);
		verify(dev.library()).vkAllocateMemory(dev, expected, null, dev.factory().pointer());
	}

	@DisplayName("The allocator falls back to the minimal requirements if the optimal memory type is not available")
	@Test
	void fallback() {
		props = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_VISIBLE)
        		.optimal(VkMemoryProperty.HOST_COHERENT)
        		.build();

		assertEquals(fallback, allocator.allocate(reqs, props).type());
	}

	@DisplayName("Memory cannot be allocated if there are no matching memory types included in the filter")
	@Test
	void none() {
		reqs.memoryTypeBits = 0;
		assertThrows(AllocationException.class, () -> allocator.allocate(reqs, props));
	}

	@DisplayName("Memory cannot be allocated if there are no matching memory types")
	@Test
	void match() {
		props = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_COHERENT)
        		.build();

		assertThrows(AllocationException.class, () -> allocator.allocate(reqs, props));
	}

	@DisplayName("The allocator fails if the hardware cannot allocate memory")
	@Test
	void failed() {
		final var info = new VkMemoryAllocateInfo() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		when(dev.library().vkAllocateMemory(dev, info, null, dev.factory().pointer())).thenReturn(999);
		assertThrows(AllocationException.class, () -> allocator.allocate(reqs, props));
	}

	@DisplayName("Memory cannot be allocated if the total number of allocations supported by the hardware is exceeded")
	@Test
	void max() {
		allocator.allocate(reqs, props);
		assertThrows(AllocationException.class, () -> allocator.allocate(reqs, props));
	}

	@DisplayName("The total number of allocations can be reset")
	@Test
	void reset() {
		allocator.allocate(reqs, props);
		allocator.reset();
		assertEquals(0, allocator.count());
		allocator.allocate(reqs, props);
	}
}
