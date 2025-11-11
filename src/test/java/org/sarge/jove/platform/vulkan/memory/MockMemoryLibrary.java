package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;

class MockMemoryLibrary extends MockVulkanLibrary {
	public boolean fail;

	@SuppressWarnings("resource")
	@Override
	public VkResult vkAllocateMemory(LogicalDevice device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, Pointer pMemory) {
		assertNotNull(device);

		if(fail) {
			throw new VulkanException(VkResult.ERROR_OUT_OF_DEVICE_MEMORY);
		}

		final var allocator = Arena.ofAuto();
		final MemorySegment memory = allocator.allocate(pAllocateInfo.allocationSize);
		pMemory.set(new Handle(memory));

		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkMapMemory(LogicalDevice device, DeviceMemory memory, long offset, long size, int flags, Pointer ppData) {
		assertNotNull(device);
		assertEquals(0, flags);

		final MemorySegment segment = memory
				.handle()
				.address()
				.asSlice(offset, size);

		ppData.set(new Handle(segment));

		return VkResult.SUCCESS;
	}
}
