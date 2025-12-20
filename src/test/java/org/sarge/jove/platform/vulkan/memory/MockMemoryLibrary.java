package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanException;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.MockLibrary;

public class MockMemoryLibrary extends MockLibrary implements MemoryLibrary {
	public boolean fail;

	@Override
	public VkResult vkAllocateMemory(LogicalDevice device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, Pointer pMemory) {
		assertNotNull(device);

		if(fail) {
			throw new VulkanException(VkResult.VK_ERROR_OUT_OF_DEVICE_MEMORY);
		}

		final int length = (int) pAllocateInfo.allocationSize;
		final var memory = MemorySegment.ofArray(new byte[length]);
		pMemory.set(memory);

		return VkResult.VK_SUCCESS;
	}

	@Override
	public VkResult vkMapMemory(LogicalDevice device, DeviceMemory memory, long offset, long size, int flags, Pointer ppData) {
		assertNotNull(device);
		assertEquals(0, flags);

		final var mapped = MemorySegment.ofArray(new byte[(int) size]);
		ppData.set(mapped);

		return VkResult.VK_SUCCESS;
	}

	@Override
	public void vkFreeMemory(LogicalDevice device, DeviceMemory memory, Handle pAllocator) {
	}

	@Override
	public void vkUnmapMemory(LogicalDevice device, DeviceMemory memory) {
	}
}
