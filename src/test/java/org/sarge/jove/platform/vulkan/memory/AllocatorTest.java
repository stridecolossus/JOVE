package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class AllocatorTest {
	private Allocator allocator;
	private MemoryType fallback, optimal;
	private VkMemoryRequirements requirements;
	private MemoryProperties<?> properties;
	private LogicalDevice device;
	private MockMemoryLibrary library;

	@BeforeEach
	void before() {
		// Create some memory types
		final Heap heap = new Heap(0, Set.of());
		fallback = new MemoryType(0, heap, Set.of(VkMemoryProperty.HOST_VISIBLE));
		optimal = new MemoryType(1, heap, Set.of(VkMemoryProperty.HOST_VISIBLE, VkMemoryProperty.DEVICE_LOCAL));

		// Init a memory request matching all types
		requirements = new VkMemoryRequirements();
		requirements.size = 3;
		requirements.memoryTypeBits = 0b11;

		// Init memory properties for the optimal type
		properties = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_VISIBLE)
        		.optimal(VkMemoryProperty.DEVICE_LOCAL)
        		.build();

		// Create allocator
		library = new MockMemoryLibrary();
		device = new MockLogicalDevice(library) {
			@Override
			public DeviceLimits limits() {
				final var limits = new VkPhysicalDeviceLimits();
				limits.maxMemoryAllocationCount = 1;
				limits.bufferImageGranularity = 2;
				return new DeviceLimits(limits);
			}
		};
		allocator = new Allocator(device, new MemoryType[]{fallback, optimal});
	}

	@Test
	void configuration() {
		assertEquals(0, allocator.count());
		assertEquals(1, allocator.max());
		assertEquals(2, allocator.page());
	}

	@DisplayName("Memory can be allocated for a matching memory type")
	@Test
	void allocate() {
		final DeviceMemory memory = allocator.allocate(requirements, properties);

		System.out.println(memory.handle());
		System.out.println(memory.handle().address());

		assertEquals(optimal, memory.type());
		assertEquals(3, memory.size());
		assertEquals(false, memory.isDestroyed());
		assertEquals(1, allocator.count());
	}

	@DisplayName("The allocator falls back to the minimal requirements if the optimal memory type is not available")
	@Test
	void fallback() {
		properties = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_VISIBLE)
        		.optimal(VkMemoryProperty.HOST_COHERENT)
        		.build();

		assertEquals(fallback, allocator.allocate(requirements, properties).type());
	}

	@DisplayName("Memory cannot be allocated if there are no matching memory types included in the filter")
	@Test
	void none() {
		requirements.memoryTypeBits = 0;
		assertThrows(AllocationException.class, () -> allocator.allocate(requirements, properties));
	}

	@DisplayName("Memory cannot be allocated if there are no matching memory types")
	@Test
	void match() {
		properties = new MemoryProperties.Builder<VkImageUsageFlag>()
        		.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
        		.required(VkMemoryProperty.HOST_COHERENT)
        		.build();

		assertThrows(AllocationException.class, () -> allocator.allocate(requirements, properties));
	}

	@DisplayName("The allocator fails if the hardware cannot allocate memory")
	@Test
	void failed() {
		library.fail = true;
		assertThrows(AllocationException.class, () -> allocator.allocate(requirements, properties));
	}

	@DisplayName("Memory cannot be allocated if the total number of allocations supported by the hardware is exceeded")
	@Test
	void max() {
		allocator.allocate(requirements, properties);
		assertThrows(AllocationException.class, () -> allocator.allocate(requirements, properties));
	}
}
