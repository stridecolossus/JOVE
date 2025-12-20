package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockAllocator extends Allocator {
	public static final MemoryType MEMORY_TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryPropertyFlags.HOST_VISIBLE));

	public MockAllocator(LogicalDevice device) {
		final var selector = new MemorySelector(new MemoryType[]{MEMORY_TYPE});
		super(device, selector, 1024, Integer.MAX_VALUE);
	}

	public MockAllocator() {
		this(new MockLogicalDevice(new MockMemoryLibrary()));
	}

	@Override
	public DeviceMemory allocate(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		return new MockDeviceMemory(requirements.size);
	}
}
