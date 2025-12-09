package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockAllocator extends Allocator {
	public static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryPropertyFlags.HOST_VISIBLE));

	public MockAllocator() {
		this(new MockLogicalDevice());
	}

	public MockAllocator(LogicalDevice device) {
		super(device, new MemoryType[]{TYPE});
	}

	@Override
	public DeviceMemory allocate(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		return new MockDeviceMemory(requirements.size);
	}
}
