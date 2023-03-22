package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockAllocator extends Allocator {
	public static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryProperty.HOST_VISIBLE));

	public MockAllocator() {
		super(new MockDeviceContext(), new MemoryType[]{TYPE}, 999, 1);
	}

	@Override
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		return new MockDeviceMemory();
	}
}
