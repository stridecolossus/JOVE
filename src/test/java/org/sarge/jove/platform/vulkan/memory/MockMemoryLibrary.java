package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanException;
import org.sarge.jove.platform.vulkan.core.*;

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
//		pMemory.set(new Handle(memory));

//		final MemorySegment p = allocator.allocate(AddressLayout.ADDRESS);
//		p.set(AddressLayout.ADDRESS, 0L, memory);
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

//		final var allocator = Arena.ofAuto();
//		final var seq = MemoryLayout.sequenceLayout(size, ValueLayout.JAVA_BYTE);
//		final AddressLayout layout = AddressLayout.ADDRESS.withTargetLayout(seq);
//		final MemorySegment p = allocator.allocate(layout);
//		p.set(layout, 0L, segment);
		ppData.set(new Handle(segment));

		//ppData.set(new Handle(segment));

		return VkResult.SUCCESS;
	}
}
