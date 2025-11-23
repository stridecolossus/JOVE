package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanException;
import org.sarge.jove.platform.vulkan.core.*;

public class MockMemoryLibrary extends MockVulkanLibrary {
	public boolean fail;

	@Override
	public VkResult vkAllocateMemory(LogicalDevice device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, Pointer pMemory) {
		assertNotNull(device);

		if(fail) {
			throw new VulkanException(VkResult.ERROR_OUT_OF_DEVICE_MEMORY);
		}

		final int length = (int) pAllocateInfo.allocationSize;
		final var memory = MemorySegment.ofArray(new byte[length]);
		pMemory.set(memory);

		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkMapMemory(LogicalDevice device, DeviceMemory memory, long offset, long size, int flags, Pointer ppData) {
		assertNotNull(device);
		assertEquals(0, flags);

		final var mapped = MemorySegment.ofArray(new byte[(int) size]);
		ppData.set(mapped);

		return VkResult.SUCCESS;
	}
}
