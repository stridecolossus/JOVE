package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class AllocatorTest {
	private Allocator allocator;
	private MemoryType type;
	private VkMemoryRequirements requirements;
	private MemoryProperties<?> properties;
	private MockMemoryLibrary library;

	@BeforeEach
	void before() {
		// Create some memory types
		type = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryPropertyFlags.HOST_VISIBLE));

		// Init memory properties matching the memory type
		properties = new MemoryProperties.Builder<VkImageUsageFlags>()
        		.usage(VkImageUsageFlags.COLOR_ATTACHMENT)
        		.required(VkMemoryPropertyFlags.HOST_VISIBLE)
        		.build();

		// Init a memory allocation
		requirements = new VkMemoryRequirements();
		requirements.size = 42;
		requirements.memoryTypeBits = 0x1;

		// Create allocator
		library = new MockMemoryLibrary();
		final var device = new MockLogicalDevice(library);
		final var selector = new MemorySelector(new MemoryType[]{type});
		allocator = new Allocator(device, selector, 1024, 1);
	}

	@Test
	void constructor() {
		assertEquals(0, allocator.count());
	}

	@DisplayName("Memory can be allocated for a matching memory type")
	@Test
	void allocate() {
		final DeviceMemory memory = allocator.allocate(requirements, properties);
		assertEquals(type, memory.type());
		assertEquals(42, memory.size());
		assertEquals(false, memory.isDestroyed());
		assertEquals(1, allocator.count());
	}

	@DisplayName("Memory cannot be allocated if no memory type matches the allocation")
	@Test
	void none() {
		requirements.memoryTypeBits = 0;
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
