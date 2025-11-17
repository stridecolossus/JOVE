package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockAllocator extends Allocator {
	public static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryProperty.HOST_VISIBLE));

	public MockAllocator() {
		final var device = new MockLogicalDevice() {
			@Override
			public DeviceLimits limits() {
				final var limits = new VkPhysicalDeviceLimits();
				limits.maxMemoryAllocationCount = -1;
				limits.bufferImageGranularity = 1024;
				return new DeviceLimits(limits);
			}
		};
		super(device, new MemoryType[]{TYPE});
	}

	@Override
	public DeviceMemory allocate(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		return new MockDeviceMemory(requirements.size);
	}
}
